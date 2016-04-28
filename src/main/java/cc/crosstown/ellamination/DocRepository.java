package cc.crosstown.ellamination;

import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.data.rest.core.annotation.RepositoryRestResource;



import cc.crosstown.dhl.model.Doc;

//@RepositoryRestResource(collectionResourceRel = "doc", path = "doc")
public interface DocRepository extends MongoRepository<Doc, String> {

	Doc findByName(String name);

}