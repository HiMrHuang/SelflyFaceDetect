package com.aee.huanghao.selflypreview.widget;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aee.huanghao.selflypreview.R;


/**
 * Created by huanghao
 * Created Time: 2017/6/20
 * Version code: 1.0
 * Description:
 */

public class RockerView extends RelativeLayout implements View.OnTouchListener{

	private Context mContext;

	private View rootView;
	private RelativeLayout rl_joystick;
	private RelativeLayout rl_root;
	private ImageView iv_smallRound;
	private ImageView iv_bigRound;
	private TextView iv_top;
	private TextView iv_bottom;
	private ImageView iv_left;
	private ImageView iv_right;
	private TextView mTv_g;

	private int[] location = new int[2];
	private int[] rootView_location = new int[2];
	private int[] bigRound_location = new int[2];
	private float ivCenterX;
	private float ivCenterY;
	private int space;
	private boolean isReverse = false;
	private boolean isRockerMode = true;
	private float smallRoundLocationX;
	private float smallRoundLocationY;
	private float iv_smallRoundRadius; //白圆的半径
	private float iv_bigRoundRadius; //底盘大圆的半径
	private int iv_bigRoundWidth;
	private int locationFlag = 0;
	private float criticalX;
	private float criticalY;
	private int coordinatesX = 0;
	private int coordinatesY = 0;

	private boolean lockYAxis = false;

	private NewSingleRudderListener listener;

	private int rockType = 0;
	private FingerPressListener pressListener;


	public RockerView(Context context) {
		this(context,null);
	}

	public RockerView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public RockerView(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr,0);
	}

	public RockerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		mContext = context;
		init();
	}

	private Vibrator vibrator;


	private void init() {

		vibrator = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);

		rootView = View.inflate(mContext, R.layout.rocker_layout,this);
		rl_root = (RelativeLayout) rootView.findViewById(R.id.rl_root);
		rl_joystick = (RelativeLayout) rootView.findViewById(R.id.rl_joystick);

		iv_top = (TextView) rootView.findViewById(R.id.iv_up);
		iv_bottom = (TextView) rootView.findViewById(R.id.iv_down);
		iv_left = (ImageView) rootView.findViewById(R.id.iv_left);
		iv_right = (ImageView) rootView.findViewById(R.id.iv_right);

		iv_smallRound = (ImageView) rootView.findViewById(R.id.iv_small);
		iv_bigRound = (ImageView) rootView.findViewById(R.id.iv_big);

		setOnTouchListener(this);

		iv_smallRound.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				iv_smallRound.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				iv_smallRound.getLocationOnScreen(location);
				iv_bigRound.getLocationOnScreen(bigRound_location);
				rl_root.getLocationOnScreen(rootView_location);

				iv_bigRoundWidth = iv_bigRound.getWidth();

				iv_smallRoundRadius = (float) ((iv_smallRound.getWidth() * 1.0) / 2);
				iv_bigRoundRadius = (float) ((iv_bigRoundWidth * 1.0) / 2);
				smallRoundLocationX = iv_smallRound.getX();
				smallRoundLocationY = iv_smallRound.getY();

				//圆心点坐标
				ivCenterX = smallRoundLocationX + iv_smallRoundRadius;
				ivCenterY = smallRoundLocationY + iv_smallRoundRadius;

				space = bigRound_location[0] - rootView_location[0];
			}
		});

	}


	public void setOnFingerPressListener(FingerPressListener pressListener){
		this.pressListener = pressListener;
	}

	public interface FingerPressListener{
		void onFingerPress(boolean isPress, int locationFlag);
	}

	public void setFourIcon(){
		iv_top.setText(R.string.forward);
		iv_bottom.setText(R.string.back);
		iv_left.setImageResource(R.mipmap.left_icons);
		iv_right.setImageResource(R.mipmap.right_icons);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				float currentLocationX0 = event.getRawX() - (location[0] + iv_smallRoundRadius);
				float currentLocationY0 = event.getRawY() - (location[1] + iv_smallRoundRadius);
				criticalX = currentLocationX0;
				criticalY = currentLocationY0;
				if((Math.pow(currentLocationX0, 2) + Math.pow(currentLocationY0, 2)) > (Math.pow(iv_bigRoundRadius, 2))){
					float angle = (float) (Math.atan(currentLocationY0 / currentLocationX0) * 180 / Math.PI);
					if (currentLocationX0 < 0) {
						angle += 180;
					}
					angle = (float) (angle * Math.PI / 180.0);

					if(Math.abs(currentLocationX0) > iv_bigRoundRadius){
						currentLocationX0 = (Math.abs(currentLocationX0) / currentLocationX0) * iv_bigRoundRadius;
					}

					if(Math.abs(currentLocationY0) > iv_bigRoundRadius){
						currentLocationY0 = (Math.abs(currentLocationY0) / currentLocationY0) * iv_bigRoundRadius;
					}

					criticalX = (float) (Math.cos(angle) * (iv_bigRoundRadius));
					criticalY = (float) (Math.sin(angle) * (iv_bigRoundRadius));
				}

				iv_smallRound.animate().x(criticalX + ivCenterX - iv_smallRoundRadius)
						.y(criticalY + ivCenterY - iv_smallRoundRadius).setDuration(0).start();
				break;

			case MotionEvent.ACTION_MOVE:
				float moveX = event.getRawX();
				float currentLocationX = event.getRawX() - (location[0] + iv_smallRoundRadius);
				float currentLocationY = event.getRawY() - (location[1] + iv_smallRoundRadius);

				criticalX = currentLocationX;
				criticalY = currentLocationY;

				if((Math.pow(currentLocationX, 2) + Math.pow(currentLocationY, 2)) > (Math.pow(iv_bigRoundRadius, 2))){

					float angle = (float) (Math.atan(currentLocationY / currentLocationX) * 180 / Math.PI);
					if (currentLocationX < 0) {
						angle += 180;
					}
					angle = (float) (angle * Math.PI / 180.0);

					if(Math.abs(currentLocationX) > iv_bigRoundRadius){
						currentLocationX = (Math.abs(currentLocationX) / currentLocationX) * iv_bigRoundRadius;

					}

					if(Math.abs(currentLocationY) > iv_bigRoundRadius){
						currentLocationY = (Math.abs(currentLocationY) / currentLocationY) * iv_bigRoundRadius;
					}

					criticalX = (float) (Math.cos(angle) * (iv_bigRoundRadius));
					criticalY = (float) (Math.sin(angle) * (iv_bigRoundRadius));
				}

				iv_smallRound.animate().x(criticalX + ivCenterX - iv_smallRoundRadius)
						.y(criticalY + ivCenterY - iv_smallRoundRadius).setDuration(0).start();

				if(isReverse){
					coordinatesX = (int) (255 - (256 * 1.0 / iv_bigRoundWidth) * (currentLocationX + ivCenterX - space));
					coordinatesY = (int) (255 - (256 * 1.0 / iv_bigRoundWidth) * (currentLocationY + ivCenterY - space));
				}else{
					coordinatesX = (int) ((256 * 1.0 / iv_bigRoundWidth) * (currentLocationX + ivCenterX - space));
					coordinatesY = (int) (255 - (256 * 1.0 / iv_bigRoundWidth) * (currentLocationY + ivCenterY - space));
				}
				if(listener != null){
					listener.onNewSteeringWheelChanged(rockType, 0, coordinatesX, coordinatesY);
				}
				break;

			case MotionEvent.ACTION_UP:
				if(lockYAxis){
					iv_smallRound.animate().x(smallRoundLocationX).setDuration(0).start();
					if(listener != null){
						listener.onNewSteeringWheelChanged(locationFlag, 0, 128, coordinatesY);
					}
				}else{
					iv_smallRound.animate().x(smallRoundLocationX).y(smallRoundLocationY).setDuration(0).start();
					if(listener != null){
						listener.onNewSteeringWheelChanged(rockType, 0, 128, 128);
					}
				}
				break;

			default:
				break;
		}
		return true;
	}


	// 回调接口
	public interface NewSingleRudderListener {
		/**
		 * @param rockerType
		 *            摇杆类型 0：左侧 1：右侧
		 * @param action
		 *            摇杆动作
		 * @param x
		 *            x坐标
		 * @param y
		 *            y坐标
		 */
		void onNewSteeringWheelChanged(int rockerType, int action, int x, int y);
	}

	/**
	 * 设置摇杆类型 0：左侧 1：右侧
	 *
	 * @param rockeType
	 */
	public void setRockeType(int rockeType){
		this.rockType = rockeType;
	}

	// 设置回调接口
	public void setSingleRudderListener(NewSingleRudderListener listener) {
		this.listener = listener;
	}


	public void  setReverse(boolean isReverse){
		this.isReverse = isReverse;
	}

}
