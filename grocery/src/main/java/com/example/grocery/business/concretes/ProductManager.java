package com.example.grocery.business.concretes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.grocery.business.abstracts.CategoryService;
import com.example.grocery.business.abstracts.ProductService;
import com.example.grocery.core.utilities.business.BusinessRules;
import com.example.grocery.core.utilities.exceptions.BusinessException;
import com.example.grocery.core.utilities.modelMapper.ModelMapperService;
import com.example.grocery.core.utilities.results.DataResult;
import com.example.grocery.core.utilities.results.Result;
import com.example.grocery.core.utilities.results.SuccessDataResult;
import com.example.grocery.core.utilities.results.SuccessResult;
import com.example.grocery.dataAccess.abstracts.ProductRepository;
import com.example.grocery.entity.concretes.Product;
import com.example.grocery.webApi.requests.product.CreateProductRequest;
import com.example.grocery.webApi.requests.product.DeleteProductRequest;
import com.example.grocery.webApi.requests.product.UpdateProductRequest;
import com.example.grocery.webApi.responses.product.GetAllProductResponse;
import com.example.grocery.webApi.responses.product.GetByIdProductResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProductManager implements ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ModelMapperService modelMapperService;

    @Override
    public Result add(CreateProductRequest createProductRequest) {

        Result rules = BusinessRules.run(isExistName(createProductRequest.getName()),
                isExistCategoryId(createProductRequest.getCategoryId()));

        Product addProduct = modelMapperService.getModelMapper().map(createProductRequest, Product.class);
        // aşağıdaki şeyi anlamaya çalış!
        addProduct = updateCategory(addProduct,
                createProductRequest.getCategoryId());
        productRepository.save((addProduct));
        log.info("added product: {} logged to file!", createProductRequest.getName());
        return new SuccessResult("Product added.");
    }

    @Override
    public Result update(UpdateProductRequest updateProductRequest, int id) {

        Product inDbProduct = productRepository.findById(id).orElseThrow(() -> new BusinessException("Id not found!"));

        Result rules = BusinessRules.run(isExistName(updateProductRequest.getName()), isExistId(id),
                isExistCategoryId(updateProductRequest.getCategoryId()));

        Product product = modelMapperService.getModelMapper().map(updateProductRequest, Product.class);
        product.setId(inDbProduct.getId());
        log.info("modified product : {} logged to file!", updateProductRequest.getName());
        productRepository.save(product);

        return new SuccessResult("Product modified.");
    }

    @Override
    public Result delete(DeleteProductRequest deleteProductRequest) {

        Result rules = BusinessRules.run(isExistId(deleteProductRequest.getId()));
        removeExpiratedProduct();

        Product product = modelMapperService.getModelMapper().map(deleteProductRequest, Product.class);

        Product productForLog = productRepository.findById(deleteProductRequest.getId())
                .orElseThrow(() -> new BusinessException("Id not found!"));
        log.info("removed product: {} logged to file!", productForLog.getName());
        productRepository.delete(product);

        return new SuccessResult("Product has been deleted.");
    }

    @Override
    public DataResult<List<GetAllProductResponse>> getAll() {
        List<GetAllProductResponse> returnList = new ArrayList<>();
        List<Product> productList = productRepository.findAll();
        for (Product product : productList) {
            Product product1 = productRepository.findById(product.getId()).get();
            GetAllProductResponse addFields = modelMapperService.getModelMapper().map(product,
                    GetAllProductResponse.class);
            addFields.setCategoryId(product1.getCategory().getId());
            returnList.add(addFields);
        }
        return new SuccessDataResult<List<GetAllProductResponse>>(returnList, "Products listed.");
    }

    @Override
    public DataResult<GetByIdProductResponse> getById(int id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            // return new ErrorDataResult<>("Id not found!");
            throw new BusinessException("Id not found!");
        }
        GetByIdProductResponse getByIdProductResponse = modelMapperService.getModelMapper().map(product,
                GetByIdProductResponse.class);
        getByIdProductResponse.setCategoryId(product.getCategory().getId());
        return new SuccessDataResult<>(getByIdProductResponse, "Product listed");
    }

    // yorum bekle...
    private Product updateCategory(Product product, int id) {
        product.setCategory(categoryService.getId(id));
        return product;
    }

    private void removeExpiratedProduct() {
        for (Product product : productRepository.findAll()) {
            if (product.getExpirationDate().isBefore(LocalDate.now())) {
                productRepository.delete(product);
            }
        }
    }

    private Result isExistId(int id) {
        if (!productRepository.existsById(id)) {
            throw new BusinessException("Id not found!");
        }
        return new SuccessResult();
    }

    private Result isExistName(String name) {
        if (productRepository.existsByName(name)) {
            log.error("product name: {} couldn't saved", name);
            throw new BusinessException("Product name can not be repeated!");
        }
        return new SuccessResult();
    }

    // olmayan kategori id'leri ayıkla
    private Result isExistCategoryId(int categoryId) {
        if (categoryService.getId(categoryId) == null) {
            throw new BusinessException("Category id not found!");
        }
        return new SuccessResult();
    }

}