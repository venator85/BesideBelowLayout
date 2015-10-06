package eu.alessiobianchi.besidebelowlayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import eu.alessiobianchi.library.R;

public class BesideBelowLayout extends ViewGroup {

	private int insideHorizMargin;
	private int insideVertMargin;
	private boolean alignRightWhenBelow;

	private boolean layoutBeside;

	public BesideBelowLayout(Context context) {
		super(context);
	}

	public BesideBelowLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0, 0);
	}

	public BesideBelowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr, 0);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public BesideBelowLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs, defStyleAttr, defStyleRes);
	}

	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BesideBelowLayout, defStyleAttr, defStyleRes);
		try {
			insideHorizMargin = a.getDimensionPixelOffset(R.styleable.BesideBelowLayout_internal_h_margin, 0);
			insideVertMargin = a.getDimensionPixelOffset(R.styleable.BesideBelowLayout_internal_v_margin, 0);
			alignRightWhenBelow = a.getBoolean(R.styleable.BesideBelowLayout_align_right_when_below, true);
		} finally {
			a.recycle();
		}
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		if (widthMode == MeasureSpec.UNSPECIFIED) {
			throw new IllegalArgumentException("Width spec cannot be UNSPECIFIED");
		}

		final int desiredWidth = MeasureSpec.getSize(widthMeasureSpec);
		final int globalAvailableWidth = desiredWidth - getPaddingLeft() - getPaddingRight();
		if (globalAvailableWidth < 0) {
			throw new IllegalArgumentException("Invalid negative width");
		}

		if (getChildCount() != 2) {
			throw new IllegalArgumentException("BesideBelowLayout must contain exactly two children");
		}

		layoutBeside = false;

		View left = getChildAt(0);
		View right = getChildAt(1);

		int leftWidthSpec = MeasureSpec.makeMeasureSpec(globalAvailableWidth - insideHorizMargin, MeasureSpec.AT_MOST);
		int leftHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

		left.measure(leftWidthSpec, leftHeightSpec);

		int totalHeight = 0;
		int rightAvailableWidth = globalAvailableWidth - left.getMeasuredWidth() - insideHorizMargin;

		if (rightAvailableWidth > 0) { // is there any room at all beside for right child?
			int rightWidthSpec = MeasureSpec.makeMeasureSpec(globalAvailableWidth, MeasureSpec.AT_MOST);
			int rightHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
			right.measure(rightWidthSpec, rightHeightSpec);

			if (right.getMeasuredWidth() < rightAvailableWidth) {
				// right view fits the space, we can put the views beside!
				totalHeight = getPaddingTop() + Math.max(left.getMeasuredHeight(), right.getMeasuredHeight()) + getPaddingBottom();
				layoutBeside = true;
			}
		}

		if (!layoutBeside) {
			leftWidthSpec = MeasureSpec.makeMeasureSpec(globalAvailableWidth, MeasureSpec.AT_MOST);
			leftHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
			left.measure(leftWidthSpec, leftHeightSpec);

			int rightWidthSpec = MeasureSpec.makeMeasureSpec(globalAvailableWidth, MeasureSpec.AT_MOST);
			int rightHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
			right.measure(rightWidthSpec, rightHeightSpec);

			totalHeight = getPaddingTop() + left.getMeasuredHeight() + insideVertMargin + right.getMeasuredHeight() + getPaddingBottom();
		}

		setMeasuredDimension(desiredWidth, totalHeight);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (!changed) {
			return;
		}

		View left = getChildAt(0);
		View right = getChildAt(1);

		left.layout(
				getPaddingLeft(),
				getPaddingTop(),
				getPaddingLeft() + left.getMeasuredWidth(),
				getPaddingTop() + left.getMeasuredHeight());

		if (layoutBeside) {
			right.layout(
					getMeasuredWidth() - getPaddingRight() - right.getMeasuredWidth(),
					getPaddingTop(),
					getMeasuredWidth() - getPaddingRight(),
					getPaddingTop() + right.getMeasuredHeight());
		} else {
			if (alignRightWhenBelow) {
				right.layout(
						getMeasuredWidth() - getPaddingRight() - right.getMeasuredWidth(),
						getPaddingTop() + left.getMeasuredHeight() + insideVertMargin,
						getMeasuredWidth() - getPaddingRight(),
						getPaddingTop() + left.getMeasuredHeight() + insideVertMargin + right.getMeasuredHeight());
			} else {
				right.layout(
						getPaddingLeft(),
						getPaddingTop() + left.getMeasuredHeight() + insideVertMargin,
						getPaddingLeft() + right.getMeasuredWidth(),
						getPaddingTop() + left.getMeasuredHeight() + insideVertMargin + right.getMeasuredHeight());
			}
		}
	}

}
