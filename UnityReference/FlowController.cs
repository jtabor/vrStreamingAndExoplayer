using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Networking;

public class FlowController : NetworkBehaviour {

	const string stage1Url = "https://storage.googleapis.com/video_vr/tiles-big/VIDEO_0065-tiles.mpd";
	const string stage2Url = "https://storage.googleapis.com/video_vr/tiles-big/VIDEO_0065-tiles.mpd";
	const string stage3Url = "https://storage.googleapis.com/video_vr/tiles-big/VIDEO_0065-tiles.mpd";
	const string stage4Url = "https://storage.googleapis.com/video_vr/tiles-big/VIDEO_0065-tiles.mpd";
	const string stage5Url = "https://storage.googleapis.com/video_vr/tiles-big/VIDEO_0065-tiles.mpd";
	const string stage6Url = "https://storage.googleapis.com/video_vr/tiles-big/VIDEO_0065-tiles.mpd";
	const string stage7Url = "https://storage.googleapis.com/video_vr/tiles-big/VIDEO_0065-tiles.mpd";

	const int STATE_INIT = 0;
	const int STATE_STAGE1 = 1;
	const int STATE_STAGE2 = 2;
	const int STATE_STAGE2_INTERACTION = 3;
	const int STATE_STAGE3 = 4;
	const int STATE_STAGE3_INTERACTION = 5;
	const int STATE_STAGE4 = 6;
	const int STATE_STAGE4_INTERACTION = 7;
	const int STATE_STAGE5 = 8;
	const int STATE_STAGE6 = 9;
	const int STATE_STAGE6_INTERACTION = 10;
	const int STATE_STAGE7 = 11;
	const int STATE_STAGE7_INTERACTION = 12;
	public GameObject testObj;
	GameObject[] currentInteractionObjects;//this holds all the object we're currently using
	public struct CameraInfo{
		public Vector3 camPos;
		public Vector3 camDir;
		public string playerId;
	}
	bool isServer = false;
	bool isHost = false;
	bool isClient = false;

	int currentState = STATE_INIT;

	// Use this for initialization
	void Start () {

	}
	public override void OnStartClient(){

		Debug.Log("CLIENT STARTED!!");

	}


	// Update is called once per frame
	void Update () {
		#if !UNITY_EDITOR && UNITY_ANDROID
		/*
		Debug.Log("Tried to set color of object.");
		GrabableScript gs = testObj.GetComponent<GrabableScript> ();
		gs.CmdChangeColor (0);
		*/
		#endif

	}

		

	void playAudio(){  //This function should play audio for the instructions of each stage

	}

	void completeInteraction(){ //This function should be called when the interaction is done.
		switch (currentState){ //Destory all the objects involved with this interaction.
		case STATE_STAGE2_INTERACTION: //solder paste interaction

			break;
		case STATE_STAGE3_INTERACTION: //pick and place interaction

			break;
		case STATE_STAGE4_INTERACTION: //magnification inspection interaction

			break;
		case STATE_STAGE6_INTERACTION: //camera inspection

			break;
		case STATE_STAGE7_INTERACTION: //test the board

			break;
		default:  //catch all the bad cases.
			Debug.Log ("ERROR: State not found in completeInteraction!!");
			break;
		}
	}

	void initInteraction(){ //This function spawns the objects for an interaction.
		switch (currentState){ //Destory all the objects involved with this interaction.
		case STATE_STAGE2_INTERACTION: //solder paste interaction

			break;
		case STATE_STAGE3_INTERACTION: //pick and place interaction

			break;
		case STATE_STAGE4_INTERACTION: //magnification inspection interaction

			break;
		case STATE_STAGE6_INTERACTION: //camera inspection

			break;
		case STATE_STAGE7_INTERACTION: //test the board

			break;
		default:  //catch all the bad cases.
			Debug.Log ("ERROR: State not found in initInteraction!!");
			break;
		}
	}

	public int getState(){
		return currentState;
	}
}
