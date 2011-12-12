package engine.easy.indexer;

/**
 * This is a IndexBuilder interface which used as generic template for new index builder.
 * 
 * Author: Adnan Urooj
 * 
 */
import java.io.IOException;

public interface IndexBuilder {

	public void createIndexes(String dataBankDirPath, String indexDirPath) throws IOException;
}
