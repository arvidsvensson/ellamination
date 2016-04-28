package cc.crosstown.ellamination;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
//import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import cc.crosstown.dhl.model.Piece;

//@RepositoryRestResource(collectionResourceRel = "piece", path = "piece")
public interface PieceRepository extends MongoRepository<Piece, String> {
	@Query("{$or:[{waybill:{$regex:?0}},{pieceNormalized:{$regex:?0}}]}")
	List<Piece> findByWaybillOrPieceNormalizedRegex(String pattern);
	
	@Query("{$or:[{waybill:?0},{pieceNormalized:?0}]}")
	Piece findByWaybillOrPieceNormalized(String what);

	@Query("{$and:[{waybill:?0},{pieceNormalized:?1}]}")
	Piece findByWaybillAndPieceNormalized(String waybill, String pieceNormalized);
	
	List<Piece> findByWaybill(String waybillScan);

	Piece findByPieceNormalized(String pieceScan);
}