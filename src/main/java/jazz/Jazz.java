package jazz;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.SwingUtilities;

/**
 * The Jazz main class, used to create Jazz windows and adjust the global Jazz
 * configuration.
 * 
 * @author Julian Fleischer
 */
public final class Jazz {

	/**
	 * Jazz does have a global random generator which is seeded with a specific
	 * seed. It is intended that a program always yields the same results.
	 * 
	 * If you want to randomize the initial seed value, use {@link #seed()} in
	 * the beginning of your program.
	 * 
	 * All classes dealing with random values in Jazz should use one of the
	 * methods generating random values provided by this class (i.e. use this
	 * global random generator).
	 */
	static private final Random r = new Random(4711337);

	/**
	 * Always use OpenGL (this is a hint, i.e. if open gl is not available Java
	 * will automatically fall back to something else).
	 */
	static {
		System.setProperty("sun.java2d.opengl", "true");
	}

	/**
	 * Prevent instantiation of this class, as it features only static methods.
	 */
	private Jazz() {

	}

	/**
	 * Sets the global random seed value.
	 * 
	 * Jazz does have its own global random generator which is initially seeded
	 * using a specific seed. Use this function to alter the seed.
	 * 
	 * This method is thread-safe.
	 * 
	 * @param seed
	 *            The new seed value.
	 */
	public static void seed(long seed) {
		synchronized (r) {
			r.setSeed(seed);
		}
	}

	/**
	 * Sets the global random seed value randomly (i.e. using
	 * {@link System#nanoTime()}).
	 * 
	 * Jazz does have its own global random generator which is initially seeded
	 * using a specific seed. Use this function to randomize the initial setup.
	 * 
	 * This method is thread-safe.
	 */
	public static void seed() {
		seed(System.nanoTime());
	}

	/**
	 * Generates a random integer value using the global random generator.
	 * 
	 * This method is thread-safe.
	 * 
	 * @return A random integer value (any integer value is possible).
	 */
	public static int randomInt() {
		synchronized (r) {
			return r.nextInt();
		}
	}

	/**
	 * Generates a random integer value between 0 (inclusive) and the specified
	 * maximum value (exclusive). <code>randomInt(3)</code> will generate one of
	 * zero, one, or two.
	 * 
	 * This method will return zero if you pass it a value less than one.
	 * 
	 * @param upto
	 *            The maximum number (exclusive).
	 * @return An integer value between 0 (inclusive) and <code>upto</code>
	 *         (exclusive) or 0 if <code>upto</code> was less than 1.
	 */
	public static int randomInt(int upto) {
		if (upto >= 1) {
			synchronized (r) {
				return r.nextInt(upto);
			}
		} else {
			return 0;
		}
	}

	public static int randomInt(int from, int upto) {
		return randomInt(upto - from) + from;
	}

	/**
	 * Shuffles a list (in-place).
	 * 
	 * The list is shuffled using the global random generator offered by Jazz.
	 * 
	 * @param list
	 *            The list to shuffle. Note that the list needs to be modifiable
	 *            since it is shuffled in-place.
	 */
	public static void shuffle(List<?> list) {
		Collections.shuffle(list, r);
	}

	/**
	 * Shuffles an array (in-place).
	 * 
	 * The array is shuffled using the global random generator offered by Jazz.
	 * 
	 * @param array
	 *            The array to shuffle.
	 */
	public static void shuffle(Object[] array) {
		// asList uses the backing array as store,
		// therefore changes to the list are reflected by the array.
		Collections.shuffle(Arrays.asList(array), r);
	}

	/**
	 * Shuffles an int-array (in-place).
	 * 
	 * The array is shuffled using the global random generator offered by Jazz.
	 * 
	 * @param array
	 *            The array to shuffle.
	 */
	public static void shuffle(int[] array) {
		// asList uses the backing array as store,
		// therefore changes to the list are reflected by the array.
		Collections.shuffle(Arrays.asList(array), r);
	}

	/**
	 * Shuffles a double-array (in-place)
	 * 
	 * The array is shuffled using the global random generator offered by Jazz.
	 * 
	 * @param array
	 *            The array to shuffle.
	 */
	public static void shuffle(double[] array) {
		Collections.shuffle(Arrays.asList(array), r);
	}

	/**
	 * Displays a static picture in a single window.
	 * 
	 * @param title
	 *            The title of the displayed window.
	 * @param width
	 *            The width of the displayed window.
	 * @param height
	 *            The height of the displayed window.
	 * @param picture
	 *            The picture to be drawn in the displayed window.
	 * @return A reference to the newly created window.
	 */
	public static Window display(final String title, int width, int height,
			final Picture picture) {
		// Use an animation object to display, as it allows for zooming and
		// panning.
		return play(title, width, height, new Animation() {

			@Override
			public Picture getPicture() {
				return picture;
			}

			@Override
			public void update(double time, double delta) {
				// do nothing, the picture is static.
			}
		});
	}

	public static Window displayFullscreen(String title, Picture picture) {
		return display(title, 0, 0, picture);
	}

	public static Window animate(final String title, int a, int b,
			final Animation animation) {
		return play(title, a, b, animation);
	}

	public static <M> Window animate(final String title, int a, int b,
			final M w, final Renderer<M> r, final UpdateHandler<M> u) {
		return animate(title, a, b, new Animation() {
			M world = w;

			@Override
			public void update(double time, double delta) {
				u.update(world, time, delta);
			}

			@Override
			public Picture getPicture() {
				return r.render(world);
			}
		});
	}

	public static Window animateFullscreen(String title, Animation animation) {
		return play(title, 0, 0, animation);
	}

	public static Window play(final String title, int a, int b,
			final World world) {
		return play(title, a, b, (Model) world);
	}

	public static <M> Window play(final String title, int a, int b,
			final M world, final Renderer<M> r, final UpdateHandler<M> u,
			final EventHandler<M> e) {
		return play(title, a, b, new DelegatingWorld<M>(world, r, u, e));
	}

	public static <M> Window playFullscreen(final String title, int a, int b,
			final M world, final Renderer<M> r, final UpdateHandler<M> u,
			final EventHandler<M> e) {
		return play(title, 0, 0, new DelegatingWorld<M>(world, r, u, e));
	}

	public static Window playFullscreen(String title, World world) {
		return play(title, 0, 0, (Model) world);
	}

	/**
	 * INTERNAL: 
	 * 
	 * @param title
	 * @param width
	 * @param height
	 * @param model
	 * @return
	 */
	static WindowImpl play(final String title, final int width, final int height,
			final Model model) {
		final WindowImpl window = new WindowImpl();

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					final MainWindow mainWindow = new MainWindow(title, model,
							window, width, height);
					final GraphicsDevice device = GraphicsEnvironment
							.getLocalGraphicsEnvironment()
							.getDefaultScreenDevice();
					if ((width <= 0 || height <= 0) && device.isFullScreenSupported()) {
						device.setFullScreenWindow(mainWindow);
					} else {
						mainWindow.setVisible(true);
					}
				}
			});
		} catch (InvocationTargetException | InterruptedException exc) {

		}
		return window;
	};
}
