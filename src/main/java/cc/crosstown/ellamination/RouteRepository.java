package cc.crosstown.ellamination;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
//import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import cc.crosstown.dhl.model.Route;

//@RepositoryRestResource(collectionResourceRel = "route", path = "route")
public interface RouteRepository extends MongoRepository<Route, String> {
	List<Route> findByNameContains(String what);
	@Query("{name : {$regex : ?0}}")
	List<Route> findByNameRegex(String regex);
}