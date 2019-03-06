using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class TestExoplayer : MonoBehaviour {
	public Camera exoCamera;
	private ExoplayerScript exoScript;

	public const int STATE_WAITING_TO_START = 0;
	public const int STATE_PLAYING = 1;
	public const int STATE_WAITING_TO_STOP = 2;
	 	

	public GameObject board;
	public GameObject components;
	public GameObject paster;
	public GameObject exoSphere;

	//JOSH added for paper
	float prevTime = 0;

	public bool alreadyQueuedVideo = false;
	bool isWaitingForPads = false;
	bool isWaitingForParts = false;
	bool alreadyDidInteraction = false;
	/*private string[] videoUrls = {"http://storage.googleapis.com/video_vr/final-videos/newIntro-tiles.mpd",
		"http://storage.googleapis.com/video_vr/final-videos/newV1-tiles.mpd",
		"http://storage.googleapis.com/video_vr/final-videos/newV2a-tiles.mpd",
		"http://storage.googleapis.com/video_vr/final-videos/newV2b-tiles.mpd", //Do an interaction here!
		"http://storage.googleapis.com/video_vr/final-videos/newV4-tiles.mpd",
		"http://storage.googleapis.com/video_vr/final-videos/newV3a-tiles.mpd",
		"http://storage.googleapis.com/video_vr/final-videos/newV3b-tiles.mpd", //Do an interaction here!
		"http://storage.googleapis.com/video_vr/final-videos/newV5a-tiles.mpd",
		"http://storage.googleapis.com/video_vr/final-videos/newV5b-tiles.mpd"};*/

	private string[] videoUrls = { "http://pages.cs.wisc.edu/~tabor/bunny_test_1080_60-tiles.mpd" };

	private int currentVideo = 0;
	// Use this for initialization
	void Start () {
		changeVisibility (board, false);
		changeVisibility (paster, false);
		changeVisibility (components, false);

		exoScript = exoCamera.GetComponent<ExoplayerScript> ();
	}
	void changeVisibility(GameObject parent, bool isVisible){
		Renderer[] renderers = parent.GetComponentsInChildren<Renderer> ();
		foreach (Renderer r in renderers) {
			r.enabled = isVisible;
		}
	}
	// Update is called once per frame
	void Update () {
		if (isWaitingForPads) {
			bool allGray = true;
			GameObject[] gos = GameObject.FindGameObjectsWithTag ("pad");
			for (int i = 0; i < gos.Length; i++) {
				if (!gos [i].GetComponent<PadScript> ().isGray) {
					allGray = false;
					break;
				}
			}
			if (allGray == true) {
				changeVisibility (board, false);
				changeVisibility (paster, false);
				changeVisibility (exoSphere, true);
				isWaitingForPads = false;
				alreadyDidInteraction = true;
			}
			return;
		}
		if (isWaitingForParts) {
			bool allSet = true;
			GameObject[] gos = GameObject.FindGameObjectsWithTag ("tool");
			for (int i = 0; i < gos.Length; i++) {
				if (!gos [i].GetComponent<GrabableScript>().isFinished) {
					allSet = false;
					break;
				}
			}
			if (allSet == true) {
				changeVisibility (board, false);
				changeVisibility (components, false);
				changeVisibility (exoSphere, true);
				isWaitingForParts = false;
				alreadyDidInteraction = true;
			}
			return;
		}
		if (exoScript.getState() == ExoplayerScript.STATE_INIT) {
			if (!alreadyQueuedVideo) {
				exoScript.queueNextVideo (videoUrls [currentVideo]);
				currentVideo++;
				alreadyQueuedVideo = true;
				prevTime = Time.time;
			}
		}
		if (exoScript.getState() == ExoplayerScript.STATE_WAITING) {
			alreadyQueuedVideo = false;
			alreadyDidInteraction = false;
			float newTime = Time.time;
			exoScript.playVideo ();
			if ((newTime - prevTime) > 10) {
				
			}
		}
		if (exoScript.getState () == ExoplayerScript.STATE_DONE) {
			//JOSH: ADDED FOR PAPER:
			if (currentVideo == 1) {
				return;
			}
			if (currentVideo == 4 && !alreadyDidInteraction) {
				isWaitingForPads = true;
				changeVisibility (board, true);
				changeVisibility (paster, true);
				changeVisibility (exoSphere, false);
				alreadyDidInteraction = true;
				return;
			} else if (currentVideo == 7 && !alreadyDidInteraction) {
				isWaitingForParts = true;
				changeVisibility (board, true);
				changeVisibility (components, true);
				changeVisibility (exoSphere, false);
				alreadyDidInteraction = true;
				return;
			}
			if (!alreadyQueuedVideo) {
				exoScript.queueNextVideo (videoUrls [currentVideo]);
				currentVideo++;
				alreadyQueuedVideo = true;
			}
		}
//			
	}
}
