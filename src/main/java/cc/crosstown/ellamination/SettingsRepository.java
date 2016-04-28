package cc.crosstown.ellamination;

import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import cc.crosstown.common.Settings;

//@RepositoryRestResource(collectionResourceRel = "doc", path = "doc")
public interface SettingsRepository extends MongoRepository<Settings, String> {
	// empty
}