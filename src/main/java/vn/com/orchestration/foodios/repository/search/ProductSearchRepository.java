package vn.com.orchestration.foodios.repository.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import vn.com.orchestration.foodios.document.ProductDoc;

import java.util.List;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDoc, String> {
    List<ProductDoc> findByNameContaining(String name);

    org.springframework.data.domain.Page<ProductDoc> findByNameContainingIgnoreCase(String name, org.springframework.data.domain.Pageable pageable);
}
