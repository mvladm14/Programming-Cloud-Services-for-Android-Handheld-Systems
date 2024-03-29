/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import retrofit.http.Multipart;
import retrofit.http.Streaming;

@Controller
public class VideoSvcController {

	private static final AtomicLong currentId = new AtomicLong(0L);

	private Map<Long, Video> videos = new HashMap<Long, Video>();

	private VideoFileManager videoDataMgr;

	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it to
	 * something other than "AnEmptyController"
	 * 
	 * 
	 * ________ ________ ________ ________ ___ ___ ___ ________ ___ __ |\
	 * ____\|\ __ \|\ __ \|\ ___ \ |\ \ |\ \|\ \|\ ____\|\ \|\ \ \ \ \___|\ \
	 * \|\ \ \ \|\ \ \ \_|\ \ \ \ \ \ \ \\\ \ \ \___|\ \ \/ /|_ \ \ \ __\ \ \\\
	 * \ \ \\\ \ \ \ \\ \ \ \ \ \ \ \\\ \ \ \ \ \ ___ \ \ \ \|\ \ \ \\\ \ \ \\\
	 * \ \ \_\\ \ \ \ \____\ \ \\\ \ \ \____\ \ \\ \ \ \ \_______\ \_______\
	 * \_______\ \_______\ \ \_______\ \_______\ \_______\ \__\\ \__\
	 * \|_______|\|_______|\|_______|\|_______|
	 * \|_______|\|_______|\|_______|\|__| \|__|
	 * 
	 * 
	 */

	/**
	 * M�todo para recuperar la direcci�n local del servidor
	 * 
	 * @return Cadena con la direcci�n local del servidor
	 */
	private String getUrlBaseForLocalServer() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes()).getRequest();
		String base = "http://"
				+ request.getServerName()
				+ ((request.getServerPort() != 80) ? ":"
						+ request.getServerPort() : "");
		return base;
	}

	/**
	 * M�todo para almacenar los datos del video al objeto video correspondiente
	 * 
	 * @param v
	 *            Video donde se almacenar�n los datos
	 * @param videoData
	 *            Datos del video
	 * @throws IOException
	 *             Excepci�n
	 */
	public void saveSomeVideo(Video v, MultipartFile videoData)
			throws IOException {
		videoDataMgr.saveVideoData(v, videoData.getInputStream());
	}

	/**
	 * M�todo para copiar los datos del video al flujo de salida especificado
	 * 
	 * @param v
	 *            Video de donde se leer�n los datos
	 * @param response
	 *            Objeto response que almacenar� los datos del video
	 * @throws IOException
	 *             Excepci�n
	 */
	public void serveSomeVideo(Video v, HttpServletResponse response)
			throws IOException {
		videoDataMgr.copyVideoData(v, response.getOutputStream());
	}

	/**
	 * M�todo que nos permite almacenar un video en la estructura de datos del
	 * programa
	 * 
	 * @param entity
	 *            Video a almacenar
	 * @return Video almacenado
	 */
	public Video save(Video entity) {
		checkAndSetId(entity);
		videos.put(entity.getId(), entity);
		return entity;
	}

	/**
	 * M�todo que nos permite asignar un identificador el Video
	 * 
	 * @param entity
	 *            Video al que se le asignar� el identificador
	 */
	private void checkAndSetId(Video entity) {
		if (entity.getId() == 0) {
			entity.setId(currentId.incrementAndGet());
		}
	}

	/**
	 * M�todo que nos permite recuperar la lista de videos almacenados
	 * 
	 * @return Lista de videos almacenados
	 */
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
	public @ResponseBody
	Collection<Video> getVideoList() {

		// Devolvemos los valores del hashmap donde almacenamos los videos
		return videos.values();
	}

	/**
	 * M�todo que nos permite almacenar un video
	 * 
	 * @param v
	 *            El video a almacenar
	 * @return El video almacenado
	 */
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody
	Video addVideo(@RequestBody Video v) {

		// Almacenamos el video
		save(v);

		// Le asignamos la url donde est� almacenado la informaci�n del video
		v.setDataUrl(getUrlBaseForLocalServer() + "/video/" + v.getId()
				+ "/data");

		// Devolvemos el video con la url almacenada
		return v;
	}

	/**
	 * M�todo que nos permite asignar la informaci�n del video a un objeto video
	 * espec�fico
	 * 
	 * @param id
	 *            Id del video
	 * @param videoData
	 *            Informaci�n del video a asignar
	 * @param response
	 *            Respuesta de la petici�n
	 * @return Estatus del video
	 * @throws IOException
	 *             Excepci�n
	 */
	@Multipart
	@RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.POST)
	public @ResponseBody
	VideoStatus setVideoData(@PathVariable("id") long id,
			final @RequestParam("data") MultipartFile videoData,
			HttpServletResponse response) throws IOException {

		// Creamos un objeto VideoFileManager
		videoDataMgr = VideoFileManager.get();

		// Cambiamos el estado del video a procesando
		VideoStatus state = new VideoStatus(VideoState.PROCESSING);

		// Si existe un objeto video almacenado que corresponde con el id
		// especificado
		if (videos.get(id) != null) {
			// Almacenamos la informaci�n del video en el mismo
			saveSomeVideo(videos.get(id), videoData);

			// Cambiamos el estado del video a preparado
			state.setState(VideoStatus.VideoState.READY);

		} else {
			// Si no existe, asignamos el valor correspondiente a la respuesta
			// del procedimiento
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}

		// Devolvemos el estado del video
		return state;
	}

	/**
	 * M�todo que nos permite recuperar la informaci�n de video un video
	 * espec�fico
	 * 
	 * @param id
	 *            Identificador del video
	 * @param response
	 *            Respuesta de la petici�n
	 * @throws IOException
	 *             Excepci�n
	 */
	@Streaming
	@RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.GET)
	public @ResponseBody
	void getData(@PathVariable("id") long id, HttpServletResponse response)
			throws IOException {

		// Comprobamos si existe un video con el identificador expecificado
		if (videos.get(id) != null) {

			// Si existe, lo recuperamos
			Video v = videos.get(id);

			// Y copiamos la informaci�n del video al flujo de respuesta
			serveSomeVideo(v, response);
		} else {
			// Si no existe, asignamos el valor correspondiente a la respuesta
			// del procedimiento
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}
}
