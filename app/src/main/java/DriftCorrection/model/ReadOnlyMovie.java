package DriftCorrection.model;

import java.nio.file.Path;

// read only wrapper class of Movie for Synchroniser
public class ReadOnlyMovie {
	private final Movie myMovie;
	
	public ReadOnlyMovie(Movie movie) {
		this.myMovie = movie;
	}
	
	public int getState() {
		return myMovie.getState();
	}

	public int getSegementStart(int frameNumber) {
		MovieSegment segment = getMovieSegment(frameNumber);
		return segment.getStartFrame();
	}

	public int getSegementEnd(int frameNumber) {
		MovieSegment segment = getMovieSegment(frameNumber);
		return segment.getEndFrame();
	}

	public Path getframe(int frameNumber) {
		return myMovie.getFileList().get(frameNumber);
	}
	
	public Path getROIFrame(int frameNumber) {
		MovieSegment segment = getMovieSegment(frameNumber);
		int ROIFrame = segment.getROIFrame();
		if (ROIFrame == -1) {
			return null;
		}
		return myMovie.getFileList().get(ROIFrame);
	}

	public int[] getROI(int frameNumber) {
		MovieSegment segment = getMovieSegment(frameNumber);
		return segment.getROI();
	}
	
	private MovieSegment getMovieSegment(int frameNumber) {
		return myMovie.getMovieSegment(frameNumber);
	}
}
