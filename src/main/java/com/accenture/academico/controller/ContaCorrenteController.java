package com.accenture.academico.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.accenture.academico.exceptions.InsufficientFundsException;
import com.accenture.academico.model.ContaCorrente;
import com.accenture.academico.model.Extrato;
import com.accenture.academico.service.ContaCorrenteService;
import com.accenture.academico.service.ExtratoService;


@RestController
public class ContaCorrenteController {

	@Autowired
	ContaCorrenteService contaCorrenteService;
	
	@Autowired
	ExtratoService extratoService;
	 
	@GetMapping("/contaCorrente")
	private List<ContaCorrente> getAllContaCorrente() {
		return contaCorrenteService.getAllContaCorrente();
	}
	
	@GetMapping("/contaCorrente/{id}")
	private ContaCorrente getContaCorrente(@PathVariable("id") int id) {
		return contaCorrenteService.getContaCorrenteById(id);
	}
	
	@DeleteMapping("/contaCorrente/{id}")
	private void deleteContaCorrente(@PathVariable("id") int id) {
		contaCorrenteService.delete(id);
	}
	
	@PostMapping("/contaCorrente")
	private int saveContaCorrente(@RequestBody ContaCorrente contaCorrente) {
		contaCorrenteService.saveOrUpdate(contaCorrente);
		return contaCorrente.getIdContaCorrente();
	}
	
	@PostMapping("/contaCorrente/{id}/saque/{valor}")
	private void sacar(@PathVariable("id") int id, @PathVariable("valor") Double valorSaque) {
		ContaCorrente conta = contaCorrenteService.getContaCorrenteById(id);
		Double valorConta = conta.getSaldo();
		
		if (valorSaque <= valorConta) {
			
			conta.setSaldo(valorConta - valorSaque);
			
			Extrato extrato = new Extrato();
			LocalDateTime dataHora = LocalDateTime.now();
			
			extrato.setDataHoraMovimento(dataHora);
			extrato.setOperacao(Extrato.SAQUE);
			extrato.setContaCorrente(conta);
			extrato.setValor(valorSaque);

			extratoService.saveOrUpdate(extrato);
			contaCorrenteService.saveOrUpdate(conta);
		}else {
			throw new InsufficientFundsException("Não foi possível sacar. Fundos insuficientes.");
		}
	}
	
	@PostMapping("/contaCorrente/{id}/deposito/{valor}")
	private void depositar(@PathVariable("id") int id, @PathVariable("valor") Double valorDeposito) {
		ContaCorrente conta = contaCorrenteService.getContaCorrenteById(id);
		Double valorConta = conta.getSaldo();
		
		conta.setSaldo(valorConta + valorDeposito);
		
		Extrato extrato = new Extrato();
		LocalDateTime dataHora = LocalDateTime.now();
		
		extrato.setDataHoraMovimento(dataHora);
		extrato.setOperacao(Extrato.DEPOSITO);
		extrato.setContaCorrente(conta);
		extrato.setValor(valorDeposito);

		extratoService.saveOrUpdate(extrato);
		contaCorrenteService.saveOrUpdate(conta);
	}
}

