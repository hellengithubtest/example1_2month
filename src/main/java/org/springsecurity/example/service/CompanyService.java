package org.springsecurity.example.service;

import org.springsecurity.example.model.Company;
import org.springsecurity.example.model.Order;
import org.springsecurity.example.repository.CompanyRepository;
import org.springsecurity.example.repository.OrderRepository;
import org.springsecurity.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Company> getAll() {
        return companyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Company> getAllByUserId(long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User does not exist"));
        return user.getCompanies();
    }

    @Transactional(readOnly = true)
    public Company getById(long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company does not exist"));
    }

    @Transactional
    public Company createCompany(Company company, long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User does not exist"));
        var companySave = companyRepository.save(company);
        user.getCompanies().add(company);
        userRepository.save(user);
        return companySave;
    }

    @Transactional
    public Company updateCompany(long id, String name) {
        var company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company does not exist"));
        company.setName(name);
        return companyRepository.save(company);
    }

    @Transactional(readOnly = true)
    public List<Order> getCompanyOrders(long companyId) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company does not exist"));
        return company.getOrders();
    }

    @Transactional
    public Order createOrder(long companyId, Order order) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company does not exist"));
        order.setCustomer(company);
        return orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(long companyId, long orderId) {
        // Yes, companyId is not used now
        orderRepository.deleteById(orderId);
    }
}
