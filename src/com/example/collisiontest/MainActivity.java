package com.example.collisiontest;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.shape.RectangularShape;
import org.andengine.entity.shape.Shape;
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
import com.makersf.andengine.extension.collisions.pixelperfect.masks.MaskUtils;

public class MainActivity extends BaseGameActivity {
	
	private static final int SCREEN_HEIGHT = 320;
	private static final int SCREEN_WIDTH = 480;
	private static final int ALPHA_THERSHOLD = 0;
	
	private Camera mCamera;
	private Scene mScene;
	private BitmapTextureAtlas triangleTexture;
    private BitmapTextureAtlas diamondTexture;
    private PixelPerfectTiledTextureRegion triangleRegion;
    private PixelPerfectTextureRegion diamond100Region;
    private PixelPerfectTextureRegion diamond32Region;
    
    private PixelPerfectTextureRegion starRegion;
    
    private CollisionLogger logger;
    private Shape[] mShapes;
    
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

        this.triangleTexture = new BitmapTextureAtlas(textureManager, 2048, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        this.diamondTexture = new BitmapTextureAtlas(textureManager, 1024, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        this.triangleRegion = PixelPerfectTextureRegionFactory.createTiledFromAsset(triangleTexture, this, "spinning-triangle.png", 0, 0, 20, 1, ALPHA_THERSHOLD);
        this.diamond100Region = PixelPerfectTextureRegionFactory.createFromAsset(diamondTexture, this, "diamond-100.png", 0, 0, ALPHA_THERSHOLD);
        this.diamond32Region = PixelPerfectTextureRegionFactory.createFromAsset(diamondTexture, this, "diamond-32.png", 102, 0, ALPHA_THERSHOLD);
        this.starRegion = PixelPerfectTextureRegionFactory.createFromAsset(diamondTexture, this, "star.png", 200, 0, ALPHA_THERSHOLD);

        this.triangleTexture.load();
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
		
		final PixelPerfectSprite diamond100 = new PixelPerfectSprite(400, 100, diamond100Region, VBOmanager){
			
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
                            for (Shape B : mShapes)
        						if(this != B) {
        							logger.startCollisionCheck();
        							boolean collisionResult = this.collidesWith(B);
        							logger.endCollisionCheck(collisionResult);
        						}
                            logger.printStatistics(true);
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
            
            @Override
			public boolean collidesWith(RectangularShape pOtherShape) {
				if(super.collidesWith(pOtherShape)) {
					Log.i("COLLIDING_DIAMOND", "COLLISION!!!!");
					return true;
				}
				Log.i("COLLIDING_DIAMOND", "NO COLLISION!!!!");
				return false;
			}
        };
        final PixelPerfectSprite diamond32 = new PixelPerfectSprite(300,100, diamond32Region, VBOmanager);
        final PixelPerfectSprite star = new PixelPerfectSprite(0, 50, starRegion, VBOmanager);
        
        mScene.attachChild(diamond100);
        mScene.registerTouchArea(diamond100);
        mScene.attachChild(diamond32);
        mScene.attachChild(star);
        
        MaskUtils.writeMaskToSDCard(diamond100.getPixelPerfectMask(), "/collisiontest", "diamond100");
        MaskUtils.writeMaskToSDCard(diamond32.getPixelPerfectMask(), "/collisiontest", "diamond32");
        MaskUtils.writeMaskToSDCard(star.getPixelPerfectMask(), "/collisiontest", "star");
    	
        final PixelPerfectAnimatedSprite spinningTriangle = new PixelPerfectAnimatedSprite(150, 200, triangleRegion, VBOmanager);
        spinningTriangle.setCurrentTileIndex(9);
        spinningTriangle.animate(200);
        
        for(int i =0; i< spinningTriangle.getTileCount(); i++) {
        	MaskUtils.writeMaskToSDCard(spinningTriangle.getPixelMask(i), "/collisiontest", "triangle" + i);
        }
        
        mScene.attachChild(spinningTriangle);
        
        mShapes = new Shape[]{diamond100, diamond32, star, spinningTriangle};
        
        pOnCreateSceneCallback.onCreateSceneFinished(mScene);
		
	}

	@Override
	public void onPopulateScene(Scene pScene,
			OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		pScene.registerUpdateHandler(new IUpdateHandler() {
			
			@Override
			public void reset() {
				
			}
			
			@Override
			public void onUpdate(float pSecondsElapsed) {
				/*for(Shape A : mShapes) 
					for (Shape B : mShapes)
						if(A != B) {
							logger.startCollisionCheck();
							boolean collisionResult = A.collidesWith(B);
							logger.endCollisionCheck(collisionResult);
						}
				//logger.printStatistics(true);
				*/
			}
		});
	
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

}
