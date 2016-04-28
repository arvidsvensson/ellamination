package cc.crosstown.common;

import java.io.IOException;
import java.io.InputStream;

public interface Parser<T> {
	T parse(InputStream input) throws IOException;
}
