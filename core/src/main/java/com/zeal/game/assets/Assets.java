package com.zeal.game.assets;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized Asset loader using LibGDX AssetManager.
 * Reads `assets/assets.txt` and loads common textures/atlases at startup
 * to avoid runtime GPU uploads when creating SpriteAnimator instances.
 *
 * Also supports loading simple 3D model formats (.g3dj, .g3db, .obj) and
 * exposes them via Assets.getModel(path).
 */
public final class Assets {
	public static final AssetManager manager = new AssetManager();

	// Models loaded synchronously (ObjLoader / G3dModelLoader). We keep a simple cache
	// and dispose models explicitly in dispose(). AssetManager does not manage these.
	private static final Map<String, Model> modelCache = new HashMap<>();

	private Assets() {}

	/**
	 * Load all listed assets synchronously. Call from the GL thread during
	 * application startup (e.g. your Game.create()).
	 */
	public static void loadAll() {
		Array<String> list = readAssetsList();

		// Register model loader for .g3dj/.g3db if needed by other systems using AssetManager
		// (we still load models synchronously here into modelCache for immediate use).
		for (String path : list) {
			// Skip missing files gracefully - some projects may list optional assets.
			boolean exists = Gdx.files.internal(path).exists();
			if (!exists) {
				Gdx.app.error("Assets", "Missing asset listed in assets.txt, skipping: " + path);
				continue;
			}

			try {
				if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg")) {
					if (!manager.isLoaded(path, Texture.class)) manager.load(path, Texture.class);
				} else if (path.endsWith(".atlas")) {
					if (!manager.isLoaded(path, TextureAtlas.class)) manager.load(path, TextureAtlas.class);
				} else if (path.endsWith(".fnt")) {
					if (!manager.isLoaded(path, BitmapFont.class)) manager.load(path, BitmapFont.class);
				} else if (path.endsWith(".json") || path.endsWith(".skin")) {
					if (!manager.isLoaded(path, Skin.class)) manager.load(path, Skin.class);
				} else if (path.endsWith(".g3dj") || path.endsWith(".g3db") || path.endsWith(".obj")) {
					// Load simple 3D models synchronously into our cache.
					loadModelSync(path);
				} else {
					// ignore other files for now
				}
			} catch (Exception e) {
				Gdx.app.error("Assets", "Failed to queue asset for loading: " + path, e);
			}
		}

		// Block until finished so all textures are uploaded on the GL thread.
		try {
			manager.finishLoading();
			Gdx.app.log("Assets", "Finished loading assets: count=" + manager.getAssetNames().size);
		} catch (Exception e) {
			Gdx.app.error("Assets", "Error loading assets", e);
		}
    }
    /**
     * Queue non-model assets for asynchronous loading by AssetManager.
     * Call this on the GL thread before using update()/getProgress().
     */
    public static void queueAssets() {
        Array<String> list = readAssetsList();
        for (String path : list) {
            boolean exists = Gdx.files.internal(path).exists();
            if (!exists) continue;
            try {
                if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                    if (!manager.isLoaded(path, Texture.class)) manager.load(path, Texture.class);
                } else if (path.endsWith(".atlas")) {
                    if (!manager.isLoaded(path, TextureAtlas.class)) manager.load(path, TextureAtlas.class);
                } else if (path.endsWith(".fnt")) {
                    if (!manager.isLoaded(path, BitmapFont.class)) manager.load(path, BitmapFont.class);
                } else if (path.endsWith(".json") || path.endsWith(".skin")) {
                    if (!manager.isLoaded(path, Skin.class)) manager.load(path, Skin.class);
                }
            } catch (Exception e) {
                Gdx.app.error("Assets", "Failed to queue asset for loading: " + path, e);
            }
        }
    }

    /**
     * Advance the AssetManager loading step. Returns true when loading finished.
     */
    public static boolean update() {
        try {
            return manager.update();
        } catch (Exception e) {
            Gdx.app.error("Assets", "Error during asset update", e);
            return false;
        }
    }

    /**
     * Returns progress 0..1 for AssetManager.
     */
    public static float getProgress() {
        try { return manager.getProgress(); } catch (Exception e) { return 0f; }
    }

    /**
     * Finish AssetManager loading synchronously and then load models synchronously.
     */
    public static void finishAndLoadModelsSync() {
        try {
            manager.finishLoading();
        } catch (Exception e) {
            Gdx.app.error("Assets", "Error finishing asset loading", e);
        }
        // Load models listed in assets.txt
        Array<String> list = readAssetsList();
        for (String path : list) {
            if (path.endsWith(".g3dj") || path.endsWith(".g3db") || path.endsWith(".obj")) {
                loadModelSync(path);
            }
        }
    }
	

	private static void loadModelSync(String path) {
		try {
			if (modelCache.containsKey(path)) return;
			if (path.endsWith(".obj")) {
				ObjLoader loader = new ObjLoader();
				Model m = loader.loadModel(Gdx.files.internal(path));
				modelCache.put(path, m);
				Gdx.app.log("Assets", "Loaded OBJ model: " + path);
			} else if (path.endsWith(".g3dj") || path.endsWith(".g3db")) {
				G3dModelLoader loader = new G3dModelLoader(new JsonReader());
				Model m = loader.loadModel(Gdx.files.internal(path));
				modelCache.put(path, m);
				Gdx.app.log("Assets", "Loaded G3D model: " + path);
			} else {
				Gdx.app.log("Assets", "Unsupported model format for path: " + path);
			}
		} catch (Exception e) {
			Gdx.app.error("Assets", "Failed to load model: " + path, e);
		}
	}

	/**
	 * Read the asset list file (assets/assets.txt or assets.txt in working dir).
	 */
	private static Array<String> readAssetsList() {
		Array<String> out = new Array<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(Gdx.files.internal("assets.txt").read()))) {
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) continue;
				out.add(line);
			}
		} catch (Exception e) {
			Gdx.app.error("Assets", "Failed to read assets.txt", e);
		}
		return out;
	}

	/**
	 * Retrieve a previously loaded Model by its path from assets.txt.
	 * Returns null if not loaded or unsupported.
	 */
	public static Model getModel(String path) {
		return modelCache.get(path);
	}

	public static void dispose() {
		try { manager.dispose(); } catch (Exception ignored) {}
		// Dispose cached models
		try {
			for (Model m : modelCache.values()) {
				try { m.dispose(); } catch (Exception ignored) {}
			}
			modelCache.clear();
		} catch (Exception ignored) {}
	}
}
