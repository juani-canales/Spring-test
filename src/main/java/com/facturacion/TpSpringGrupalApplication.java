package com.facturacion;

import com.facturacion.consultas.ConsultasJPQL;
import com.facturacion.service.FacturacionReporteService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TpSpringGrupalApplication {

	public static void main(String[] args) {
		SpringApplication.run(TpSpringGrupalApplication.class, args);
	}

	//Descomentar desde @Bean para poder ver las consultar y los reportes al ejecutar el program
	/*
	@Bean
	public CommandLineRunner init(FacturacionReporteService reportes, ConsultasJPQL consultas) {
		return args -> {
			System.out.println("--- EJECUTANDO REPORTES ---");
			reportes.generarReportes();

			System.out.println("--- EJECUTANDO CONSULTAS JPQL ---");
			consultas.hacerConsultas();
		};
	}
	*/
}
