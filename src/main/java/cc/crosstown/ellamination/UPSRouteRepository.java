package cc.crosstown.ellamination;

import org.springframework.data.mongodb.repository.MongoRepository;

import cc.crosstown.ups.model.UPSRoute;
//import org.springframework.data.rest.core.annotation.RepositoryRestResource;


//@RepositoryRestResource(collectionResourceRel = "route", path = "route")
public interface UPSRouteRepository extends MongoRepository<UPSRoute, String> {
	// marker
}