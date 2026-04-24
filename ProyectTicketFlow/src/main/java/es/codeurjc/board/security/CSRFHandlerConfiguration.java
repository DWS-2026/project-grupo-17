package es.codeurjc.board.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
/**
 * Registra un interceptor MVC para exponer el token CSRF en las vistas.
 */
public class CSRFHandlerConfiguration implements WebMvcConfigurer {

	@Override
	// Anade el interceptor al pipeline MVC para todas las rutas.
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new CSRFHandlerInterceptor());
	}
}

/**
 * Interceptor que copia el token CSRF desde la request al modelo de la vista.
 */
class CSRFHandlerInterceptor implements HandlerInterceptor {

	@Override
	// Se ejecuta despues del controlador y antes de renderizar la plantilla.
	public void postHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler,
			final ModelAndView modelAndView) throws Exception {

		if (modelAndView != null) {

			CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
			if (token != null) {
				modelAndView.addObject("token", token.getToken());
			}
		}
	}
}

//el usuario abre una sesion http y se le asigna un token
//este handler guarda el token para esa sesión y se lo pasa a todos los html
//cuando se envie un post (formulario) se comprueba que el token del form coincide con el de la sesión
