package com.deliverytech.delivery.service;

import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;

    ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    /**
     * Cadastrar novo cliente
     */
    public Cliente cadastrar(Cliente cliente) {
        // Validar email único
        if (clienteRepository.existsByEmail(cliente.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado: " + cliente.getEmail());
        }

        // Validações de negócio
        validarDadosCliente(cliente);

        // Definir como ativo por padrão
        cliente.setAtivo(true);

        return clienteRepository.save(cliente);
    }

    /**
     * Buscar cliente por ID
     */
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }

    /**
     * Buscar cliente por email
     */
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email);
    }

    /**
     * Listar todos os clientes ativos
     */
    @Transactional(readOnly = true)
    public List<Cliente> listarAtivos() {
        return clienteRepository.findByAtivoTrue();
    }

    /**
     * Atualizar dados do cliente
     */
    public Cliente atualizar(Long id, Cliente clienteAtualizado) {
        Cliente cliente = buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + id));

        // Verificar se email não está sendo usado por outro cliente
        if (!cliente.getEmail().equals(clienteAtualizado.getEmail()) &&
            clienteRepository.existsByEmail(clienteAtualizado.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado: " + clienteAtualizado.getEmail());
        }

        // Atualizar campos
        cliente.setNome(clienteAtualizado.getNome());
        cliente.setEmail(clienteAtualizado.getEmail());
        cliente.setTelefone(clienteAtualizado.getTelefone());
        cliente.setEndereco(clienteAtualizado.getEndereco());

        return clienteRepository.save(cliente);
    }

    /**
     * Inativar cliente (soft delete)
     */
    public void inativar(Long id) {
        Cliente cliente = buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + id));

        if (!cliente.isAtivo()) {
        throw new IllegalStateException("Cliente já está inativo.");
        }

        cliente.inativar();
        clienteRepository.save(cliente);
    }

    /*
     * Reativar cliente
     */
    public void reativar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
        if (cliente.isAtivo()) {
            throw new IllegalArgumentException("Cliente já está ativo");
        }

        cliente.setAtivo(true);
        clienteRepository.save(cliente);
    }

    /**
     * Buscar clientes por nome
     */
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorNome(String nome) {
        return clienteRepository.findByNomeContainingIgnoreCase(nome);
    }

    /**
     * Validações de negócio
     */
    private void validarDadosCliente(Cliente cliente) {
        if (cliente.getNome() == null || cliente.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }

        if (cliente.getEmail() == null || cliente.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }

        if (cliente.getNome().length() < 2) {
            throw new IllegalArgumentException("Nome deve ter pelo menos 2 caracteres");
        }
    }
}