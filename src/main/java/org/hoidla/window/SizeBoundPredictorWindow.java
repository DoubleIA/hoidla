/*
 * hoidla: various algorithms for Big Data solutions
 * Author: Pranab Ghosh
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.hoidla.window;

import java.util.AbstractList;
import java.util.Collections;

/**
 * @author pranab
 *
 */
public class SizeBoundPredictorWindow extends SizeBoundWindow<Double> {
	private double average;
	private double sum;
	private int count;
	private double median;
	private double weightedAverage;
	private double[] weights;
	private double expSmoothingFactor;
	private boolean processed;
	private String predictor;

	public static final String PRED_AVERAGE = "average";
	public static final String PRED_MEDIAN = "median";
	public static final String PRED_WEIGHTED_AVERAGE = "weightedAverage";
	public static final String PRED_LINEAR_REGRESSION = "linearRegression";
	public static final String PRED_EXP_SMOOTHING = "expSmoothing";
	
	
	/**
	 * @param maxSize
	 */
	public SizeBoundPredictorWindow(int maxSize, String predictor) {
		super(maxSize);
		this.predictor = predictor;
	}
	
	/**
	 * @param weights
	 * @return
	 */
	public SizeBoundPredictorWindow withWeights(double[] weights) {
		if (predictor.equals(PRED_WEIGHTED_AVERAGE)) {
			this.weights = weights;
		} else {
			throw new IllegalStateException("not appropriate for the predictor type " + predictor);
		}
		return this;
	}
	
	public SizeBoundPredictorWindow withExpSmoothingFactor(double expSmoothingFactor) {
		if (predictor.equals(PRED_EXP_SMOOTHING)) {
			this.expSmoothingFactor = expSmoothingFactor;
			weights = new double[maxSize];
			double fact = expSmoothingFactor;
			double mult = 1.0 - expSmoothingFactor;
			for (int i = maxSize -1; i >= 0; --i) {
				weights[i] = fact;
				fact *= mult;
			}
		} else {
			throw new IllegalStateException("not appropriate for the predictor type " + predictor);
		}
		return this;
	}
	
	/* (non-Javadoc)
	 * @see org.hoidla.window.DataWindow#add(java.lang.Object)
	 */
	public void add(Double value) {
		processed = false;
		super.add(value);
	}
	
	/* (non-Javadoc)
	 * @see org.hoidla.window.DataWindow#processFullWindow()
	 */
	public  void processFullWindow() {
		if (predictor.equals(PRED_AVERAGE)) {
			sum =  0;
			count = 0;
			for (Double val : dataWindow) {
				sum += val;
				++count;
			}
			average = sum / count;
		} else if (predictor.equals(PRED_MEDIAN)) {
			AbstractList<Double> cloned = cloneWindow();
			Collections.sort(cloned);
			int half = cloned.size() / 2;
			if (cloned.size() % 2 == 1) {
				median = cloned.get(half);
			} else {
				median = (cloned.get(half - 1) + cloned.get(half)) / 2;
			}
		} else if (predictor.equals(PRED_WEIGHTED_AVERAGE) || predictor.equals(PRED_EXP_SMOOTHING)) {
			findWeightedAverage();
		}
		
		processed = true;
	}

	/**
	 * 
	 */
	private void findWeightedAverage() {
		sum = 0;
		for (int i = 0; i < dataWindow.size(); ++i) {
			sum += dataWindow.get(i) * weights[i];
		}
		weightedAverage = sum / dataWindow.size();
	}

	/**
	 * @return
	 */
	public boolean isProcessed() {
		return processed;
	}

	/**
	 * @param processed
	 */
	public void setProcessed(boolean processed) {
		this.processed = processed;
	}
	
	/**
	 * 
	 */
	public void forcedProcess() {
		if (!processed) {
			 processFullWindow();
		}
	}

	/**
	 * @return
	 */
	public double getAverage() {
		return average;
	}

	/**
	 * @return
	 */
	public double getMedian() {
		return median;
	}

	/**
	 * @return
	 */
	public double getWeightedAverage() {
		return weightedAverage;
	}

}
