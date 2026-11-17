package com.facturacion.service.afip;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class AfipWsaaClient {

    private static final String URL_WSAA_HOMO =
            "https://wsaahomo.afip.gov.ar/ws/services/LoginCms";
    public static String[] obtenerTokenYSign(String keyPath, String certPath) throws
        Exception {

            if (Security.getProvider("BC") == null) {
                Security.addProvider(new BouncyCastleProvider());
            }

            PrivateKey privateKey = cargarClavePrivada(keyPath);
            X509Certificate cert = cargarCertificado(certPath);
            ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            String genTime =
                    now.minusMinutes(5).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            String expTime =
                    now.plusMinutes(10).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            String traXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<loginTicketRequest version=\"1.0\">" +
                    "<header>" +
                    "<uniqueId>" + (System.currentTimeMillis() / 1000) + "</uniqueId>" +
                    "<generationTime>" + genTime + "</generationTime>" +
                    "<expirationTime>" + expTime + "</expirationTime>" +
                    "</header>" +
                    "<service>wsfe</service>" +
                    "</loginTicketRequest>";

            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            ContentSigner sha256Signer = new JcaContentSignerBuilder("SHA256withRSA")
                    .setProvider("BC").build(privateKey);

            gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder().setProvider("BC").build())
                    .build(sha256Signer, cert));

            gen.addCertificates(new JcaCertStore(Collections.singletonList(cert)));
            CMSSignedData signedData = gen.generate(new
                    org.bouncycastle.cms.CMSProcessableByteArray(
                    traXml.getBytes(StandardCharsets.UTF_8)), true);

            String cmsBase64 = Base64.getEncoder().encodeToString(signedData.getEncoded());

            String soapEnvelope = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                    "xmlns:nso=\"http://wsaa.view.sua.dvadac.desein.afip.gov\">" +
                    "<soapenv:Header/><soapenv:Body><nso:loginCms><nso:in0>" + cmsBase64 +
                    "</nso:in0></nso:loginCms></soapenv:Body></soapenv:Envelope>";

            String xmlResponse = enviarSoap(URL_WSAA_HOMO, soapEnvelope);

            // Limpieza y parsing básico de token y sign
            String xmlLimpio = xmlResponse.replace("&lt;", "<").replace("&gt;", ">");
            String token = xmlLimpio.substring(xmlLimpio.indexOf("<token>") + 7,
                    xmlLimpio.indexOf("</token>"));
            String sign = xmlLimpio.substring(xmlLimpio.indexOf("<sign>") + 6,
                    xmlLimpio.indexOf("</sign>"));

            String exp = xmlLimpio.substring(xmlLimpio.indexOf("<expirationTime>") + 16,
                    xmlLimpio.indexOf("</expirationTime>"));
            String vence = java.time.OffsetDateTime.parse(exp).toInstant().toString();

            return new String[]{token, sign, vence};   // ahora devuelve 3 datos
        }

    private static PrivateKey cargarClavePrivada(String path) throws Exception {
        // Lógica estándar para leer archivo .key PEM
        String content = new String(Files.readAllBytes(new File(path).toPath()));
        content = content.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(content);
        return KeyFactory.getInstance("RSA").generatePrivate(new
                java.security.spec.PKCS8EncodedKeySpec(keyBytes));
    }

    private static X509Certificate cargarCertificado(String path) throws Exception {
        try (FileInputStream fis = new FileInputStream(path)) {
            return (X509Certificate)
                    java.security.cert.CertificateFactory.getInstance("X.509").generateCertificate(fis);
        }
    }

    private static String enviarSoap(String urlStr, String soapXml) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        conn.setRequestProperty("SOAPAction", "\"\"");   // WSAA espera este encabezado (vacío)
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(soapXml.getBytes(StandardCharsets.UTF_8));
        }

        int codigo = conn.getResponseCode();
        // Si fue exitoso se lee el flujo normal; si no, el flujo de error (donde viene el Fault)
        InputStream flujo = (codigo >= 200 && codigo < 300) ? conn.getInputStream() : conn.getErrorStream();

        String cuerpo = "";
        if (flujo != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(flujo, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                cuerpo = sb.toString();
            }
        }

        if (codigo < 200 || codigo >= 300) {
            throw new IOException("ARCA respondió HTTP " + codigo + ": " + cuerpo);
        }
        return cuerpo;
    }

    public static String[] obtenerTokenYSignConCache(String keyPath, String certPath,
                                                     String archivoCache) throws Exception {
        java.nio.file.Path ruta = java.nio.file.Path.of(archivoCache);

        // 1) ¿Hay un pase guardado que todavía sirve?
        if (Files.exists(ruta)) {
            List<String> lineas = Files.readAllLines(ruta);
            if (lineas.size() >= 3) {
                java.time.Instant vence = java.time.Instant.parse(lineas.get(2));
                if (java.time.Instant.now().isBefore(vence.minusSeconds(300))) {  // 5 min de margen
                    return new String[]{lineas.get(0), lineas.get(1)};
                }
            }
        }

        // 2) No hay (o venció): pedimos uno nuevo y lo guardamos
        String[] ts = obtenerTokenYSign(keyPath, certPath);
        Files.write(ruta, List.of(ts[0], ts[1], ts[2]));
        return new String[]{ts[0], ts[1]};
    }
}
