package com.example.collisiontest;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.AnimatedSprite.IAnimationListener;
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
import com.makersf.andengine.extension.collisions.entity.sprite.PixelPerfectAnimatedSprite;
import com.makersf.andengine.extension.collisions.entity.sprite.PixelPerfectSprite;
import com.makersf.andengine.extension.collisions.opengl.texture.region.PixelPerfectTextureRegion;
import com.makersf.andengine.extension.collisions.opengl.texture.region.PixelPerfectTextureRegionFactory;
import com.makersf.andengine.extension.collisions.opengl.texture.region.PixelPerfectTiledTextureRegion;

public class MainActivity extends BaseGameActivity {
	
	private static final int SCREEN_HEIGHT = 320;
	private static final int SCREEN_WIDTH = 480;
	private static final int ALPHA_THERSHOLD = 0;
	
	private Camera mCamera;
	private Scene mScene;
    private BitmapTextureAtlas diamondTexture;
    
    private PixelPerfectTextureRegion starRegion;
    private PixelPerfectTiledTextureRegion trireg;
    
    private CollisionLogger logger;
    private PixelPerfectSprite star1;
    private PixelPerfectSprite star2;
    private PixelPerfectAnimatedSprite spintri;
    
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

        this.diamondTexture = new BitmapTextureAtlas(textureManager, 2048, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        this.starRegion = PixelPerfectTextureRegionFactory.createFromAsset(diamondTexture, this, "star.png", 0, 0, ALPHA_THERSHOLD);
        
        this.trireg = PixelPerfectTextureRegionFactory.createTiledFromAsset(diamondTexture, this, "spinning-triangle.png", 0, 200, 20, 1, 0);

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
			spintri.setRotation(spintri.getRotation() + 1);
			return true;
		}
    	   
       };
       
       	mScene.attachChild(star1);
       	mScene.attachChild(star2);
       	mScene.registerTouchArea(star2);
       	mScene.registerTouchArea(roatator);
       
       	spintri = new PixelPerfectAnimatedSprite(150, 200, trireg, VBOmanager);
       	spintri.animate(1000, new IAnimationListener() {
			
			@Override
			public void onAnimationStarted(AnimatedSprite pAnimatedSprite,
					int pInitialLoopCount) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationLoopFinished(AnimatedSprite pAnimatedSprite,
					int pRemainingLoopCount, int pInitialLoopCount) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationFrameChanged(AnimatedSprite pAnimatedSprite,
					int pOldFrameIndex, int pNewFrameIndex) {
				logger.startCollisionCheck();
				boolean collisionResult = spintri.collidesWith(star1);
				logger.endCollisionCheck(collisionResult);
				if(collisionResult) {
					Log.i("SPINTRI LOGGER", "TRIANGLE COLLIDES WITH STAR1");
				}
				else {
					Log.i("SPINTRI LOGGER", "TRIANGLE DO NOT COLLIDES WITH STAR1");
				}
				logger.startCollisionCheck();
				collisionResult = spintri.collidesWith(star2);
				logger.endCollisionCheck(collisionResult);
				if(collisionResult) {
					Log.i("SPINTRI LOGGER", "TRIANGLE COLLIDES WITH STAR2");
				}
				else {
					Log.i("SPINTRI LOGGER", "TRIANGLE DO NOT COLLIDES WITH STAR2");
				}
			}
			
			@Override
			public void onAnimationFinished(AnimatedSprite pAnimatedSprite) {
				// TODO Auto-generated method stub
				
			}
		});
       mScene.attachChild(spintri);
       
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
				logger.startCollisionCheck();
				boolean collisionResult1 = star1.collidesWith(spintri);
				logger.endCollisionCheck(collisionResult);
				if(collisionResult1) {
					Log.i("STAR LOGGER", "STAR1 COLLIDES WITH SPINTRI");
				}
				logger.startCollisionCheck();
				boolean collisionResult2 = star2.collidesWith(spintri);
				logger.endCollisionCheck(collisionResult);
				if(collisionResult2) {
					Log.i("STAR LOGGER", "STAR2 COLLIDES WITH SPINTRI");
				}
				logger.printStatistics(false);
			}
		});
	
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

}
