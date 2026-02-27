package org.vincentyeh.img2pdf.gui.model.util.file;


import java.io.File;
import java.io.FileFilter;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.regex.PatternSyntaxException;

/**
 * A {@link FileFilter} that accepts files whose names match a glob pattern.
 * <p>
 * Pattern matching is delegated to the NIO {@link PathMatcher} with the
 * {@code "glob:"} syntax (e.g. {@code "*.jpg"}, {@code "*.{png,jpg}"}).
 * On Windows the match is case-insensitive.
 * </p>
 */
public class GlobbingFileFilter implements FileFilter {
	private final PathMatcher matcher;
	private final String pattern;
	private static final String syntax="glob";

	/**
	 * Creates a filter that accepts files matching the given glob pattern.
	 *
	 * @param pattern the glob pattern to match against file names (e.g. {@code "*.jpg"})
	 * @throws PatternSyntaxException        if the pattern is syntactically invalid
	 * @throws UnsupportedOperationException if the default {@link FileSystem} does not
	 *                                       support the {@code "glob:"} syntax
	 * @throws IllegalArgumentException      if {@code pattern} is {@code null}
	 */
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

	/**
	 * Returns a string representation of this filter in the form {@code "glob:<pattern>"}.
	 *
	 * @return the glob syntax string used internally by the {@link PathMatcher}
	 */
	@Override
	public String toString() {
		return syntax+":"+pattern;
	}

	/**
	 * Validates that the given pattern string is not {@code null}.
	 *
	 * @param obj the pattern to check
	 * @throws IllegalArgumentException if {@code obj} is {@code null}
	 */
	private static void checkPatternNull(String obj){
		if(obj==null)
			throw new IllegalArgumentException("pattern" +"==null");
	}
}
