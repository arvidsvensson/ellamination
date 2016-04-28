package cc.crosstown.ellamination;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.data.mongodb.repository.Query;
//import org.springframework.data.rest.core.annotation.RepositoryRestResource;


import cc.crosstown.ups.model.UPSPiece;

//@RepositoryRestResource(collectionResourceRel = "piece", path = "piece")
public interface UPSPieceRepository extends MongoRepository<UPSPiece, String> {
	List<UPSPiece> findByWaybill(String waybillScan);
}