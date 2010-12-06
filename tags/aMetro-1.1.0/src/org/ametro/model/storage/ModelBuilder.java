/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 contacts@ametro.org Roman Golovanov and other
 * respective project committers (see project home page)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package org.ametro.model.storage;

import java.util.Locale;

import org.ametro.app.Constants;
import org.ametro.model.SchemeView;
import org.ametro.model.Model;
import org.ametro.util.StringUtil;

import android.util.Log;

public class ModelBuilder {

	// private static final int BUFFER_SIZE = 8196;

	private static IModelStorage getStorage(String fileName) {
		if (StringUtil.isNullOrEmpty(fileName))
			return null;
		if (fileName.toLowerCase().endsWith(".pmz"))
			return new PmzStorage();
		if (fileName.toLowerCase().endsWith(".ametro"))
			return new CsvStorage();
		return null;
	}

	public static Model loadModel(String fileName) {
		return loadModel(fileName, Locale.getDefault());
	}

	public static Model loadModel(String fileName,
			Locale locale) {
		IModelStorage storage = getStorage(fileName);
		if (storage != null) {
			try {
				long startTime = System.currentTimeMillis();
				Model model = storage.loadModel(fileName, locale);
				if (!Model.isNullOrEmpty(model)) {
					if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)) {
						Log.d(Constants.LOG_TAG_MAIN, "Model loading time is "
								+ (System.currentTimeMillis() - startTime)
								+ "ms, Provider "
								+ storage.getClass().getSimpleName());
					}
				} else {
					if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)) {
						Log.e(Constants.LOG_TAG_MAIN, "Model loading error - incorrect file, Provider " + storage.getClass().getSimpleName());
					}
				}
				return model;
			} catch (Throwable e) {
				if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)) {
					Log.e(Constants.LOG_TAG_MAIN, "Model loading error, Provider " + storage.getClass().getSimpleName(), e);
				}
			}
		}
		return null;
	}

	public static Model loadModelDescription(String fileName) {
		return loadModelDescription(fileName, Locale.getDefault());
	}

	public static Model loadModelDescription(String fileName, Locale locale) {
		IModelStorage storage = getStorage(fileName);
		if (storage != null) {
			try {
				long startTime = System.currentTimeMillis();
				Model model = storage.loadModelDescription(fileName, locale);
				if (Model.isDescriptionLoaded(model)) {
					if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)) {
						Log.d(Constants.LOG_TAG_MAIN,
							"Model description loading time is "
									+ (System.currentTimeMillis() - startTime)
									+ "ms, Provider "
									+ storage.getClass()
											.getSimpleName());
					}
				} else {
					if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)) {
						Log.e(Constants.LOG_TAG_MAIN,
								"Model description loading error - incorrect file, Provider "
										+ storage.getClass().getSimpleName());
					}
				}
				return model;
			} catch (Throwable e) {
				if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)) {
					Log.e(Constants.LOG_TAG_MAIN,
						"Model description loading error, Provider "
								+ storage.getClass().getSimpleName(), e);
				}
			}
		}
		return null;
	}

	public static void saveModel(String fileName, Model model) {
		IModelStorage storage = getStorage(fileName);
		if (storage != null) {
			try {
				long startTime = System.currentTimeMillis();
				storage.saveModel(fileName, model);
				if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)) {
					Log.d(Constants.LOG_TAG_MAIN, "Model saving time is "
							+ (System.currentTimeMillis() - startTime)
							+ "ms, Provider "
							+ storage.getClass().getSimpleName());
				}
			} catch (Throwable e) {
				if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)) {
					Log.e(Constants.LOG_TAG_MAIN,
							"Model saving error, Provider "
									+ storage.getClass().getSimpleName(), e);
				}
			}
		}
	}

	public static SchemeView loadModelView(String fileName,
			Model model, String name) {
		IModelStorage storage = getStorage(fileName);
		if (storage != null) {
			try {
				long startTime = System.currentTimeMillis();
				SchemeView view = storage.loadModelView(fileName, model,
						name);
				if (view != null) {
					if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)) {
						Log.d(Constants.LOG_TAG_MAIN, "Model view " + name
								+ " loading time is "
								+ (System.currentTimeMillis() - startTime)
								+ "ms, Provider "
								+ storage.getClass().getSimpleName());
					}
				} else {
					if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)) {
						Log.e(Constants.LOG_TAG_MAIN, "Model view " + name
								+ " not found, Provider "
								+ storage.getClass().getSimpleName());
					}

				}
				return view;
			} catch (Throwable e) {
				if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)) {
					Log.e(Constants.LOG_TAG_MAIN, "Model view " + name
							+ " loading error, Provider "
							+ storage.getClass().getSimpleName(), e);
				}
			}
		}
		return null;
	}

	public static String[] loadModelLocale(String fileName, Model model, int localeId) {
		IModelStorage storage = getStorage(fileName);
		if (storage != null) {
			try {
				long startTime = System.currentTimeMillis();
				String localeName = model.locales[localeId];
				String[] texts = storage.loadModelLocale(fileName,
						model, localeId);
				if (texts != null) {
					if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)) {
						Log.d(Constants.LOG_TAG_MAIN, "Model locale "
								+ localeName + " loading time is "
								+ (System.currentTimeMillis() - startTime)
								+ "ms, Provider "
								+ storage.getClass().getSimpleName());
					}
				} else {
					if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)) {
						Log.e(Constants.LOG_TAG_MAIN, "Model locale "
								+ localeName + " not found, Provider "
								+ storage.getClass().getSimpleName());
					}
				}
				return texts;
			} catch (Throwable e) {
				if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)) {
					Log.e(Constants.LOG_TAG_MAIN, "Model locale #" + localeId
							+ " loading error, Provider "
							+ storage.getClass().getSimpleName(), e);
				}
			}
		}
		return null;
	}

}
