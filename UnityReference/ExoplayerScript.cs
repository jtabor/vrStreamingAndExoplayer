using UnityEngine;
using System.Collections;

public class ExoplayerScript : MonoBehaviour {

	public int numberOfTiles;
	Renderer r;
	#if !UNITY_EDITOR
	AndroidJavaObject jo;
	#endif

	//	public GameObject objectToTile1;
	//	public GameObject objectToTile2;
	//	public GameObject objectToTile3;
	//	public GameObject objectToTile4;
	public Material tile1;
	public Material tile2;
	public Material tile3;
	public Material tile4;
	public Material tile5;
	public Material tile6;
	public Material tile7;
	public Material tile8;

	const int EXO_STATE_END = 4;
	const int EXO_STATE_BUFF = 2;
	const int EXO_STATE_IDLE = 1;
	const int EXO_STATE_READY = 3;
	private int exoplayerState = 0;

	public const int STATE_UNLOADED = -3;
	public const int STATE_WAITINGFORINIT = -2;
	public const int STATE_INIT = -1;
	public const int STATE_WAITING = 0;
	public const int STATE_PLAYING = 1;
	public const int STATE_DONE = 2;
	private int state = STATE_UNLOADED;

	private Texture[] textures;
	Material[] materials;
	Texture t = new Texture();

	private float speed = 45f;
	bool goThroughRouter = false;
	void Start () {
		materials = new Material[8];
		textures = new Texture[8];
		materials [0] = tile1;
		materials [1] = tile2;
		materials [2] = tile3;
		materials [3] = tile4;
		materials [4] = tile5;
		materials [5] = tile6;
		materials [6] = tile7;
		materials [7] = tile8;

	}
	void Update(){
//		transform.Rotate (Vector3.up, speed * Time.deltaTime);
	}
	// Update is called once per frame
	void OnPreRender() {
		#if !UNITY_EDITOR
		//		Debug.Log ("OnPreRender.");
		if (state != STATE_UNLOADED) {
//			Debug.Log ("state pre-unloaded");
			exoplayerState = jo.Call<int> ("getState");
		}
		switch (state) {
		case(STATE_UNLOADED):
//			Debug.Log ("state unload");
			setupExoplayer ();
			break;
		case(STATE_WAITINGFORINIT):
//			Debug.Log ("state waiting for init");
			if (exoplayerState != -1) {
				state = STATE_INIT;
			}
			//Do nothing?  Wait for start command
			break;
		case(STATE_INIT):
//			Debug.Log ("state init");
			if (exoplayerState == EXO_STATE_READY) {
				state = STATE_WAITING;
			}
			break;
		case(STATE_WAITING):
//			Debug.Log ("state waiting");
			//Do nothing?  Wait for start command
			break;
		case(STATE_PLAYING):
//			Debug.Log ("state playing");
			if (exoplayerState == EXO_STATE_READY) {
				jo.Call ("startVideo");
				jo.Call ("updateTexture");
			} else if (exoplayerState == EXO_STATE_END) {
				state = STATE_DONE;
				jo.Call ("releasePlayer");
			}

			break;
		case(STATE_DONE):
//			Debug.Log ("state done");
			break;
		default:
			Debug.LogError ("Unknown script state!");
			break;
		}



		#endif
	}
	//"https://storage.googleapis.com/video_vr/tiles-big/VIDEO_0065-tiles.mpd",

	private void setupExoplayer(){
		#if !UNITY_EDITOR
		Debug.Log ("setupExoplayer");
		state = STATE_INIT;
		AndroidJavaClass unityPlayer = new AndroidJavaClass ("com.unity3d.player.UnityPlayer");
		AndroidJavaObject androidContext = unityPlayer.GetStatic<AndroidJavaObject> ("currentActivity");
		jo = new AndroidJavaObject ("exoaartest.research.josht.exoplayerlibrarytest.SurfaceTest", numberOfTiles, androidContext, 1920, 1080, true,goThroughRouter);
		#endif
	}
//	jo.Call ("initExoplayer");
	private void setupTextures(){
		#if !UNITY_EDITOR
		Debug.Log ("setupTextures");
		int[] textureIds = jo.Call<int[]> ("getTextureIds");
		Debug.Log ("texID size: " + textureIds.Length);
		for (int i = 0; i < numberOfTiles; i++) {
			textures [i] = Texture2D.CreateExternalTexture (1920, 1080, TextureFormat.ARGB32, false, false, (System.IntPtr)textureIds [i]);
			materials[i].SetTexture("_MainTex",textures[i]);
		}
		#endif
	}
	public void queueNextVideo(string videoUrl){
		#if !UNITY_EDITOR
		Debug.Log ("queueNextVideo");
		if (state == STATE_INIT || state == STATE_DONE) {
			jo.Call ("init", videoUrl);
			setupTextures ();
			if(!goThroughRouter){
				jo.Call ("initExoplayer");
			}
			state = STATE_INIT;
		} else {
			Debug.LogError ("Tried to queue video before INIT or DONE");
		}
		#endif
	}
	public void playVideo(){
		#if !UNITY_EDITOR
		Debug.Log ("playvideo");
		if (state == STATE_WAITING) {
			state = STATE_PLAYING;
		} else {
			Debug.LogError ("Tried to start video before WAIT.");
		}
		#endif
	}
	public int getState(){

		return state;
	}

}


