package org.vincentyeh.img2pdf.model.util.file;


import java.io.File;
import java.io.FileFilter;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.regex.PatternSyntaxException;

public class GlobbingFileFilter implements FileFilter {
	private final PathMatcher matcher;
	private final String pattern;
	private static final String syntax="glob";

	public GlobbingFileFilter(String pattern)
			throws PatternSyntaxException, UnsupportedOperationException {
		checkPatternNull(pattern);

		this.pattern = pattern;
		FileSystem fs = FileSystems.getDefault();
		matcher = fs.getPathMatcher(syntax+":"+ pattern);
	}

	/**
	 * @param file The file which passed to the filter.
	 * @return Return true if file is matched for the pattern.Return false if file is invalid.
	 */
	@Override
	public boolean accept(File file) {
		return matcher.matches(file.toPath().getFileName());
	}

	@Override
	public String toString() {
		return syntax+":"+pattern;
	}

	private static void checkPatternNull(String obj){
		if(obj==null)
			throw new IllegalArgumentException("pattern" +"==null");
	}
}
