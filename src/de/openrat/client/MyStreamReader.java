package de.openrat.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class MyStreamReader extends Reader
{

	private InputStream stream;

	public MyStreamReader(InputStream inputStream)
	{
		this.stream = inputStream;
	}

	@Override
	public void close() throws IOException
	{
		stream.close();
	}

	@Override
	public int read(char[] buf, int offset, int count) throws IOException
	{
		if (count != 1 || buf.length != 1 || offset != 0)
			throw new IOException("Buffer size must be 1");
		byte[] b = new byte[1];
		this.stream.read(b);
		char c = (char) b[0];
		buf[0] = c;
		return 1;
	}

}
