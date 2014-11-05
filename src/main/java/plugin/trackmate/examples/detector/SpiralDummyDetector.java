package plugin.trackmate.examples.detector;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.Interval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class SpiralDummyDetector< T extends RealType< T > & NativeType< T >> implements SpotDetector< T >
{

	private static final double RADIAL_SPEED = 3d; // pixels per frame

	// radians per frame
	private static final double ANGULAR_SPEED = Math.PI / 10;

	// in image units
	private static final double SPOT_RADIUS = 1d;

	/** The width if the ROI. */
	private final long width;

	/** The height if the ROI. */
	private final long height;

	/** The X coordinates of the ROI. */
	private final long xstart;

	/** The Y coordinates of the ROI. */
	private final long ystart;

	/** The pixel sizes in the 3 dimensions. */
	private final double[] calibration;

	/** The frame we operate in. */
	private final int frame;

	/** Holder for the results of detection. */
	private List< Spot > spots;

	/** Error message holder. */
	private String errorMessage;

	/** Holder for the processing time. */
	private long processingTime;

	/*
	 * CONSTRUCTOR
	 */

	public SpiralDummyDetector( final Interval interval, final double[] calibration, final int frame )
	{
		// Take the ROI box from the interval parameter.
		this.width = interval.dimension( 0 );
		this.height = interval.dimension( 1 );
		this.xstart = interval.min( 0 );
		this.ystart = interval.min( 1 );
		// We will need the calibration to convert to physical units.
		this.calibration = calibration;
		// We need to know what frame we are in.
		this.frame = frame;
	}

	/*
	 * METHODS
	 */

	@Override
	public List< Spot > getResult()
	{
		return spots;
	}

	@Override
	public boolean checkInput()
	{
		// Nothing to test, it's all good.
		return true;
	}

	@Override
	public boolean process()
	{
		final long start = System.currentTimeMillis();
		spots = new ArrayList< Spot >();

		/*
		 * This dummy detector creates spots that spiral out from the center of
		 * the specified ROI. It spits a new spiral every 10 frames.
		 */

		final int x0 = ( int ) ( width / 2 + xstart );
		final int y0 = ( int ) ( height / 2 + ystart );

		int t = frame;
		int nspiral = 0;
		while ( t >= 0 )
		{
			final double r = t * RADIAL_SPEED;
			final double phi0 = nspiral * Math.PI / 4;
			final double phi = t * ANGULAR_SPEED + phi0;

			// Spot in pixel coordinates.
			final double x = x0 + r * Math.cos( phi );
			final double y = y0 + r * Math.sin( phi );

			// But we want to create spots in image coordinates:
			final double xpos = x * calibration[ 0 ];
			final double ypos = y * calibration[ 1 ];
			final double zpos = 0d;

			// Create the spot.
			final Spot spot = new Spot( xpos, ypos, zpos, SPOT_RADIUS, 1d / ( nspiral + 1d ) );
			spots.add( spot );

			// Loop to another spiral.
			t = t - 10;
			nspiral++;
		}

		final long end = System.currentTimeMillis();
		this.processingTime = end - start;
		return true;
	}

	@Override
	public String getErrorMessage()
	{
		/*
		 * If something wrong happens while you #checkInput() or #process(),
		 * state it in the errorMessage field.
		 */
		return errorMessage;
	}

	@Override
	public long getProcessingTime()
	{
		return processingTime;
	}

}
