package vn.com.orchestration.foodios.repository.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import vn.com.orchestration.foodios.document.StoreDoc;

import java.util.List;

@Repository
public interface StoreSearchRepository extends ElasticsearchRepository<StoreDoc, String> {
    java.util.List<StoreDoc> findByNameContaining(String name);

    org.springframework.data.domain.Page<StoreDoc> findByNameContainingIgnoreCase(String name, org.springframework.data.domain.Pageable pageable);
}
