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

package org.magnum.mobilecloud.video;

import java.security.Principal;
import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

@Controller
public class VideoSvcController {

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

	// Repositorio para almacenar los videos en la base de datos
	@Autowired
	private VideoRepository videos;

	/**
	 * M�todo para desear buena suerte
	 * 
	 * @return Cadena deseando buena suerte
	 */
	@RequestMapping(value = "/go", method = RequestMethod.GET)
	public @ResponseBody
	String goodLuck() {
		return "Good Luck!";
	}

	/**
	 * M�todo para a�adir v�deos
	 * 
	 * @param v
	 *            El v�deo a a�adir
	 * 
	 * @return El v�deo a�adido
	 */
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody
	Video addVideo(@RequestBody Video v) {

		// Almacenamos el v�deo en la base de datos usando el repositorio JPA
		videos.save(v);

		// Devolvemos el v�deo
		return v;
	}

	/**
	 * M�todo para recuperar la lista de v�deos
	 * 
	 * @return Una colecci�n con los v�deos almacenados
	 */
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
	public @ResponseBody
	Collection<Video> getVideoList() {

		// Buscamos todos los v�deos que haya en el respositorio y los
		// devolvemos
		return Lists.newArrayList(videos.findAll());
	}

	/**
	 * M�todo para recuperar un v�deo usando su identificador
	 * 
	 * @param id
	 *            Identificador del v�deo a buscar
	 * @return El video buscado
	 */
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}", method = RequestMethod.GET)
	public @ResponseBody
	Video getVideoById(@PathVariable("id") long id) {
		// Usamos el repositorio y buscamos un v�deo que corresponda con el id
		// introducido
		return videos.findOne(id);

	}

	/**
	 * M�todo para recuperar los v�deo usando su t�tutlo
	 * 
	 * @param title
	 *            T�tulo de los videos a buscar
	 * @return Los v�deos que correspondan con la b�squeda
	 */
	@RequestMapping(value = VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method = RequestMethod.GET)
	public @ResponseBody
	Collection<Video> findByTitle(
			@RequestParam(VideoSvcApi.TITLE_PARAMETER) String title) {
		// Usamos el repositorio y buscamos los v�deos que se correspondan con
		// el t�tulo introducido
		return videos.findByName(title);
	}

	/**
	 * M�todo para recuperar los v�deos con una duraci�n inferior a la
	 * introcudida
	 * 
	 * @param duration
	 *            Duraci�n m�xima de los v�deos a buscar
	 * @return Los v�deos que correspondan con la b�squeda
	 */
	@RequestMapping(value = VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method = RequestMethod.GET)
	public @ResponseBody
	Collection<Video> findByDurationLessThan(
			@RequestParam(VideoSvcApi.DURATION_PARAMETER) long duration) {
		// Usamos el repositorio y devolvemos los v�deos cuya duraci�n sea
		// inferior a la especificada
		return videos.findByDurationLessThan(duration);
	}

	/**
	 * M�todo por el cual podemos agregar un "me gusta" a un v�deo
	 * 
	 * @param id
	 *            Identificador del v�deo
	 * @param p
	 *            Datos del usuario autentificado
	 * @return Respusta a la petici�n ejecutada
	 */
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/like", method = RequestMethod.POST)
	public ResponseEntity<Void> likeVideo(@PathVariable("id") long id,
			Principal p) {

		// Buscamos el v�deo usando el respositorio
		Video v = videos.findOne(id);

		// Comprobamos si el v�deo existe
		if (v == null) {
			// Si no existe, devolvemos la respuesta correspondiente
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}

		// Almacenamos el nombre del usuario autentificado
		String username = p.getName();

		// Recuperamos los nombres de usuario de la gente que ha dado un
		// "me gusta" al v�deo
		//List<String> likesUsernames = v.getLikesUsernames();
		
		Set<String> likesUsernames = v.getLikesUsernames();

		// Si el nombre del usuario est� entre los nombres que han dado un
		// "me gusta" la petici�n no se puede procesar, puesto que no se puede
		// dar un "me gusta" dos veces
		if (likesUsernames.contains(username)) {
			// En este caso devolvemos la respuesta correspondiente
			return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		}

		// A�adimos el nombre de usuario autentificado a la lista de usuarios
		// que dieron un "me gusta"
		likesUsernames.add(username);

		// Asignamos la lista al v�deo
		v.setLikesUsernames(likesUsernames);

		// Asignamos el nuevo numero de "me gusta"
		v.setLikes(likesUsernames.size());

		// Usamos el repositorio para almacenar el v�deo
		videos.save(v);

		// Devolvemos la respuesta correspondiente
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	/**
	 * M�todo que no permite quitar un "me gusta" a un v�deo
	 * 
	 * @param id
	 *            Identificador del v�deo
	 * @param p
	 *            Datos del usuario autentificado
	 * @return Respuesta a la petici�n ejecutada
	 */
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/unlike", method = RequestMethod.POST)
	public ResponseEntity<Void> unlikeVideo(@PathVariable("id") long id,
			Principal p) {

		// Buscamos el v�deo correspondiente
		Video v = videos.findOne(id);

		// Verificamos si el v�deo existe
		if (v == null) {
			// Si no existe, devolvemos la respuesta correspondiente
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}

		// Almacenamos el nombre del usuario autentificado
		String userName = p.getName();

		// Recuperamos los nombres de usuario de la gente que ha dado un
		// "me gusta" al v�deo
		//List<String> userLikes = v.getLikesUsernames();
		Set<String> userLikes = v.getLikesUsernames();

		// Comprobamos si el usuario no est� en la lista de gente que ha dado
		// un "me gusta" al v�deo. De no estar, no se puede quitar su "me gusta"
		if (!userLikes.contains(userName)) {
			// En este caso, devolvemos la respuesta correspondiente
			return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		}

		// Quitamos de la lista de "me gusta" del v�deo el nombre del usuario
		// autentificado
		userLikes.remove(userName);

		// Asignamos la lista al v�deo
		v.setLikesUsernames(userLikes);

		// Reducimos el n�mero de "me gusta" del v�deo
		v.setLikes(v.getLikes() - 1);

		// Usamos el repositorio para almacenar el v�deo
		videos.save(v);

		// Devolvemos la respuesta correspondiente
		return new ResponseEntity<Void>(HttpStatus.OK);

	}

	/**
	 * M�todo que nos permite recuperar la lista de personas que han dado un
	 * "me gusta" a un v�deo
	 * 
	 * @param id
	 *            Identificador del v�deo
	 * @param response
	 *            Respuesta a la solicitud
	 * @return Lista de personas que han dado un "me gusta" a un v�deo
	 */
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/likedby", method = RequestMethod.GET)
	public @ResponseBody
	Collection<String> getListLikes(@PathVariable("id") long id,
			HttpServletResponse response) {

		// Buscamos el v�deo correspondiente
		Video v = videos.findOne(id);

		// Comprobamos si el v�deo existe
		if (v == null) {

			// Si no existe, preparamos la respuesta correspondiente
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);

			// Devolvemos nulo
			return null;
		}

		// En caso contrario, devolvemos la lista de personas que dieron un
		// "me gusta" al v�deo
		return v.getLikesUsernames();
	}

}
