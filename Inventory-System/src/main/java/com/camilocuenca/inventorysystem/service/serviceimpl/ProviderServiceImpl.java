package com.camilocuenca.inventorysystem.service.serviceimpl;

import com.camilocuenca.inventorysystem.dto.product.ProductByProviderDto;
import com.camilocuenca.inventorysystem.dto.provider.ProviderDto;
import com.camilocuenca.inventorysystem.model.Product;
import com.camilocuenca.inventorysystem.model.Provider;
import com.camilocuenca.inventorysystem.repository.ProductRepository;
import com.camilocuenca.inventorysystem.repository.ProviderRepository;
import com.camilocuenca.inventorysystem.service.serviceInterface.ProviderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProviderServiceImpl implements ProviderService {

    private final ProviderRepository providerRepository;
    private final ProductRepository productRepository;

    @Autowired
    public ProviderServiceImpl(ProviderRepository providerRepository, ProductRepository productRepository) {
        this.providerRepository = providerRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public ProviderDto createProvider(ProviderDto dto) throws Exception {
        Provider p = new Provider();
        BeanUtils.copyProperties(dto, p);
        Provider saved = providerRepository.save(p);
        ProviderDto out = new ProviderDto();
        BeanUtils.copyProperties(saved, out);
        return out;
    }

    @Override
    @Transactional
    public ProviderDto updateProvider(UUID id, ProviderDto dto) throws Exception {
        Provider existing = providerRepository.findById(id).orElseThrow(() -> new Exception("Proveedor no encontrado"));
        existing.setName(dto.getName());
        existing.setContactInfo(dto.getContactInfo());
        Provider saved = providerRepository.save(existing);
        ProviderDto out = new ProviderDto();
        BeanUtils.copyProperties(saved, out);
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderDto getProviderById(UUID id) throws Exception {
        Provider p = providerRepository.findById(id).orElseThrow(() -> new Exception("Proveedor no encontrado"));
        ProviderDto out = new ProviderDto();
        BeanUtils.copyProperties(p, out);
        return out;
    }

    @Override
    @Transactional
    public void deleteProvider(UUID id) throws Exception {
        Provider p = providerRepository.findById(id).orElseThrow(() -> new Exception("Proveedor no encontrado"));
        providerRepository.delete(p);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProviderDto> listProviders(Pageable pageable) {
        Page<Provider> page = providerRepository.findAll(pageable);
        List<ProviderDto> dtos = page.stream().map(p -> {
            ProviderDto dto = new ProviderDto();
            BeanUtils.copyProperties(p, dto);
            return dto;
        }).collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductByProviderDto> listProductsByProvider(UUID providerId, Pageable pageable) {
        Page<Product> page = productRepository.findByProviderId(providerId, pageable);
        List<ProductByProviderDto> dtos = page.stream().map(p -> {
            ProductByProviderDto dto = new ProductByProviderDto();
            dto.setProductId(p.getId());
            dto.setName(p.getName());
            dto.setSku(p.getSku());
            dto.setUnit(p.getUnit());
            return dto;
        }).collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }
}

