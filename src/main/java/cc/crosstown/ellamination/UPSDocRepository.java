package cc.crosstown.ellamination;

import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.data.rest.core.annotation.RepositoryRestResource;



import cc.crosstown.ups.model.UPSDoc;

//@RepositoryRestResource(collectionResourceRel = "doc", path = "doc")
public interface UPSDocRepository extends MongoRepository<UPSDoc, String> {

	UPSDoc findByName(String name);

}