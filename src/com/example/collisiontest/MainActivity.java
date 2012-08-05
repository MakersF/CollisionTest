package com.example.collisiontest;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.BaseGameActivity;

import android.util.Log;

import com.makersf.andengine.extension.collisions.CollisionLogger;
import com.makersf.andengine.extension.collisions.entity.sprite.PixelPerfectSprite;
import com.makersf.andengine.extension.collisions.opengl.texture.region.PixelPerfectTextureRegion;
import com.makersf.andengine.extension.collisions.opengl.texture.region.PixelPerfectTextureRegionFactory;

public class MainActivity extends BaseGameActivity {
	
	private static final int SCREEN_HEIGHT = 320;
	private static final int SCREEN_WIDTH = 480;
	private static final int ALPHA_THERSHOLD = 0;
	
	private Camera mCamera;
	private Scene mScene;
    private BitmapTextureAtlas diamondTexture;
    
    private PixelPerfectTextureRegion starRegion;
    
    private CollisionLogger logger;
    private PixelPerfectSprite star1;
    private PixelPerfectSprite star2;
    
	@Override
	public EngineOptions onCreateEngineOptions() {
		logger = new CollisionLogger();
		mCamera = new Camera(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new FillResolutionPolicy(), mCamera);
	}

	@Override
	public void onCreateResources(
			OnCreateResourcesCallback pOnCreateResourcesCallback)
			throws Exception {
		
		final TextureManager textureManager = this.getTextureManager();
		PixelPerfectTextureRegionFactory.setAssetBasePath("gfx/");
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

        this.diamondTexture = new BitmapTextureAtlas(textureManager, 1024, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        this.starRegion = PixelPerfectTextureRegionFactory.createFromAsset(diamondTexture, this, "star.png", 200, 0, ALPHA_THERSHOLD);

        this.diamondTexture.load();
        
        pOnCreateResourcesCallback.onCreateResourcesFinished();
		
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
			throws Exception {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		
		mScene = new Scene();
		mScene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));
		
		final VertexBufferObjectManager VBOmanager = this.getVertexBufferObjectManager();
		
		star2 = new PixelPerfectSprite(150, 50, starRegion, VBOmanager){
			
			boolean mGrabbed = false;
            
            @Override
            public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                switch(pSceneTouchEvent.getAction()) {
                    case TouchEvent.ACTION_DOWN:
                        this.mGrabbed = true;
                        break;
                    case TouchEvent.ACTION_MOVE:
                        if(this.mGrabbed) {
                            this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
                        }
                        break;
                    case TouchEvent.ACTION_UP:
                        if(this.mGrabbed) {
                            this.mGrabbed = false;
                        }
                        break;
                }
                return true;
            }
        };
       star1 = new PixelPerfectSprite(0, 50, starRegion, VBOmanager);
       Rectangle roatator = new Rectangle(400, 270, 80, 50, VBOmanager){

		@Override
		public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
				float pTouchAreaLocalX, float pTouchAreaLocalY) {
			star2.setRotation(star2.getRotation() + 1);
			return true;
		}
    	   
       };
       
       mScene.attachChild(star1);
       mScene.attachChild(star2);
       mScene.registerTouchArea(star2);
       mScene.registerTouchArea(roatator);
       
       pOnCreateSceneCallback.onCreateSceneFinished(mScene);
		
	}

	@Override
	public void onPopulateScene(Scene pScene,
			OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		mScene.registerUpdateHandler(new IUpdateHandler() {
			float time;
			@Override
			public void reset() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onUpdate(float pSecondsElapsed) {
				time += pSecondsElapsed;
				
				if(time <1)
					return;
				time = 0;
				
				logger.startCollisionCheck();
				boolean collisionResult = star1.collidesWith(star2);
				logger.endCollisionCheck(collisionResult);
				if(collisionResult) {
					Log.i("STAR LOGGER", "STAR1 COLLIDES WITH STAR2");
				}
				logger.printStatistics(false);
			}
		});
	
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

}
