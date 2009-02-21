package com.evancharlton.mileage.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.View;

public class LineChart extends View {
	private Paint m_axisLabelPaint;
	private Paint m_aboveAveragePaint;
	private Paint m_belowAveragePaint;
	private Paint m_axisLinesPaint;
	private Paint m_dataPaint;
	private Paint m_dataLinePaint;
	private Paint m_unitPaint;
	private Paint m_minXPaint;
	private Paint m_maxXPaint;

	private String m_verticalLabel = "";
	private String m_horizontalLabel = "";
	private String m_minXLabel = "";
	private String m_maxXLabel = "";
	private String m_minYLabel = "";
	private String m_maxYLabel = "";
	private String m_avgLabel = "";

	private boolean m_frozen = false;
	private float m_avg = 0F;
	private float m_minX = Float.MAX_VALUE;
	private float m_maxX = Float.MIN_VALUE;
	private float m_minY = Float.MAX_VALUE;
	private float m_maxY = Float.MIN_VALUE;

	private float[] m_data = null;
	private boolean m_betterOnBottom;

	public LineChart(Context context) {
		super(context);

		init();
	}

	public LineChart(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	public LineChart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init();
	}

	protected void init() {
		m_axisLabelPaint = new Paint();
		m_axisLabelPaint.setColor(0xFF999999);
		m_axisLabelPaint.setTextSize(12);
		m_axisLabelPaint.setTextAlign(Align.CENTER);
		m_axisLabelPaint.setAntiAlias(true);

		m_axisLinesPaint = new Paint();
		m_axisLabelPaint.setColor(0xFFBBBBBB);

		m_aboveAveragePaint = new Paint();
		m_aboveAveragePaint.setColor(0xDD47FF6F);

		m_belowAveragePaint = new Paint();
		m_belowAveragePaint.setColor(0x99FF3021);

		m_dataPaint = new Paint();
		m_dataPaint.setColor(Color.WHITE);
		m_dataPaint.setStrokeWidth(5);

		m_dataLinePaint = new Paint();
		m_dataLinePaint.setColor(Color.GRAY);
		m_dataLinePaint.setStrokeWidth(3);
		m_dataLinePaint.setAntiAlias(true);

		m_unitPaint = new Paint();
		m_unitPaint.setColor(Color.BLACK);
		m_unitPaint.setTextSize(12);
		m_unitPaint.setTextAlign(Align.LEFT);
		m_unitPaint.setAntiAlias(true);

		m_minXPaint = new Paint();
		m_minXPaint.setColor(0xFF999999);
		m_minXPaint.setTextSize(12);
		m_minXPaint.setTextAlign(Align.LEFT);
		m_minXPaint.setAntiAlias(true);

		m_maxXPaint = new Paint();
		m_maxXPaint.setColor(0xFF999999);
		m_maxXPaint.setTextSize(12);
		m_maxXPaint.setTextAlign(Align.RIGHT);
		m_maxXPaint.setAntiAlias(true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (!m_frozen) {
			final int width = canvas.getWidth();
			final int height = canvas.getHeight() - 50;

			if (m_minYLabel.length() == 0) {
				m_minYLabel = String.valueOf(m_minY);
			}
			if (m_maxYLabel.length() == 0) {
				m_maxYLabel = String.valueOf(m_maxY);
			}

			// axis labels
			canvas.drawText(m_horizontalLabel, width / 2, height, m_axisLabelPaint);
			canvas.save();
			canvas.translate(0, height / 2);
			canvas.rotate(90);
			canvas.drawText(m_verticalLabel, 0, 0, m_axisLabelPaint);
			canvas.restore();

			// lines
			final float axisTextHeight = m_axisLabelPaint.getTextSize();
			float[] lines = new float[] {
					axisTextHeight,
					height - axisTextHeight,
					width,
					height - axisTextHeight,
					axisTextHeight,
					0,
					axisTextHeight,
					height - axisTextHeight
			};
			canvas.drawLines(lines, m_axisLinesPaint);

			// data regions
			if (m_betterOnBottom == false) {
				canvas.drawRect(axisTextHeight + 1, 0, width, m_avg * height, m_aboveAveragePaint);
				canvas.drawRect(axisTextHeight + 1, m_avg * height, width, height - (axisTextHeight + 1), m_belowAveragePaint);
			} else {
				canvas.drawRect(axisTextHeight + 1, 0, width, m_avg * height, m_belowAveragePaint);
				canvas.drawRect(axisTextHeight + 1, m_avg * height, width, height - (axisTextHeight + 1), m_aboveAveragePaint);
			}
			canvas.drawText(m_avgLabel, axisTextHeight + 1, (m_avg * height) - 1, m_unitPaint);

			// data points
			if (m_data != null) {
				final float deltaX = m_maxX - m_minX;
				final float deltaY = m_maxY - m_minY;

				float prevX = Float.MIN_VALUE;
				float prevY = Float.MAX_VALUE;

				boolean maxRendered = false;
				boolean minRendered = false;

				for (int i = 0; i < m_data.length; i += 2) {
					final float x_raw = m_data[i];
					final float y_raw = m_data[i + 1];

					// adjust the data to fit the chart
					float x = (x_raw - m_minX) / deltaX;
					float y = (y_raw - m_minY) / deltaY;

					float posX = (float) ((x * (width - axisTextHeight)) + axisTextHeight);
					float posY = (float) ((height - axisTextHeight) - (y * (height - axisTextHeight)));

					// render the labels
					if (y_raw == m_maxY && maxRendered == false) {
						if (i < m_data.length / 4) {
							m_maxXPaint.setTextAlign(Align.LEFT);
						}
						canvas.drawText(m_maxXLabel, posX, height, m_maxXPaint);
						maxRendered = true;
					} else if (y_raw == m_minY && minRendered == false) {
						if (i > m_data.length / 4) {
							// we need to flip the alignment
							m_minXPaint.setTextAlign(Align.RIGHT);
						}
						canvas.drawText(m_minXLabel, posX, height, m_minXPaint);
						minRendered = true;
					}

					canvas.drawPoint(posX, posY, m_dataPaint);
					if (prevX != Float.MIN_VALUE && prevY != Float.MAX_VALUE) {
						canvas.drawLine(prevX, prevY, posX, posY, m_dataLinePaint);
					}
					prevX = posX;
					prevY = posY;
				}
				canvas.drawText(m_maxYLabel, axisTextHeight + 1, m_unitPaint.getTextSize(), m_unitPaint);
				canvas.drawText(m_minYLabel, axisTextHeight + 1, height - (m_unitPaint.getTextSize() + 3), m_unitPaint);
			}
		}
	}

	public void freeze() {
		m_frozen = true;
	}

	public void thaw() {
		m_frozen = false;
		invalidate();
	}

	public void setYAxisLabel(String label) {
		m_verticalLabel = label;
		invalidate();
	}

	public void setYAxisLabels(String min_label, String max_label) {
		m_minYLabel = min_label;
		m_maxYLabel = max_label;
	}

	public void setXAxisLabel(String label) {
		m_horizontalLabel = label;
		invalidate();
	}

	public void setXAxisLabels(String min_label, String max_label) {
		m_minXLabel = min_label;
		m_maxXLabel = max_label;
	}

	public void setAverageLabel(String label) {
		m_avgLabel = label;
	}

	/**
	 * Set the raw data. Note that the caller does not need to modify the data
	 * to fit the graph; this function will normalize the data appropriately.
	 * 
	 * @param data Chart data in [x0 y0 x1 y1 ... xn yn] format
	 */
	public void setDataPoints(float[] data) {
		if (data.length % 2 != 0) {
			throw new IllegalArgumentException("Invalid number of data points!");
		}

		float min_x = Float.MAX_VALUE;
		float max_x = Float.MIN_VALUE;
		float tot_x = 0F;
		float min_y = Float.MAX_VALUE;
		float max_y = Float.MIN_VALUE;
		float tot_y = 0F;
		for (int i = 0; i < data.length; i += 2) {
			float x = data[i];
			float y = data[i + 1];

			tot_x += x;
			tot_y += y;

			if (x < min_x) {
				min_x = x;
			}
			if (x > max_x) {
				max_x = x;
			}

			if (y < min_y) {
				min_y = y;
			}
			if (y > max_y) {
				max_y = y;
			}
		}

		m_minX = (float) min_x;
		m_minY = (float) min_y;
		m_maxX = (float) max_x;
		m_maxY = (float) max_y;
		m_avg = (((float) (tot_y / (data.length / 2))) - m_minY) / (m_maxY - m_minY);
		m_data = data;
	}

	public void setBetterOnBottom(boolean b) {
		m_betterOnBottom = b;
	}
}
