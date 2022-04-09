package com.smackmap.smackmapbackend.security

//import io.jsonwebtoken.ExpiredJwtException
//import io.jsonwebtoken.SignatureException
//import mu.KotlinLogging
//import org.springframework.http.HttpHeaders
//import org.springframework.http.server.ServerHttpRequest
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
//import org.springframework.security.core.context.SecurityContextHolder
//import org.springframework.security.core.userdetails.UserDetailsService
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
//import org.springframework.stereotype.Component
//import org.springframework.web.filter.OncePerRequestFilter
//import org.springframework.web.server.ServerWebExchange
//import org.springframework.web.server.WebFilter
//import org.springframework.web.server.WebFilterChain
//import reactor.core.publisher.Mono
//import javax.servlet.FilterChain
//import javax.servlet.http.HttpServletRequest
//import javax.servlet.http.HttpServletResponse

//@Component
class JwtAuthenticationFilterOld(
//    private val userDetailsService: UserDetailsService,
//    private val jwtTokenUtil: JwtTokenUtil
)
//    : OncePerRequestFilter() {
//
//    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
//        val header: String? = req.getHeader(HttpHeaders.AUTHORIZATION)
//        if (header == null) {
//            logger.warn("'${HttpHeaders.AUTHORIZATION}' header was not found in the request.")
//        } else if (!header.startsWith(TOKEN_PREFIX)) {
//            logger.warn("Could not find the Bearer prefix in header, ignoring the header.")
//        } else {
//            val authToken = header.replace(TOKEN_PREFIX, "")
//            val username = getUsernameFromToken(authToken)
//            if (username != null && SecurityContextHolder.getContext().authentication == null) {
//                validateTokenAndSetSecurityContext(req, username, authToken)
//            }
//        }
//        chain.doFilter(req, res)
//    }
//
//    private fun getUsernameFromToken(authToken: String): String? {
//        try {
//            return jwtTokenUtil.getUsernameFromToken(authToken)
//        } catch (e: IllegalArgumentException) {
//            logger.error("An error occurred during getting username from token.", e)
//        } catch (e: ExpiredJwtException) {
//            logger.warn("The token is expired and not valid anymore.", e)
//        } catch (e: SignatureException) {
//            logger.error("Authentication Failed. Username or Password not valid.")
//        }
//        return null
//    }
//
//    private fun validateTokenAndSetSecurityContext(req: HttpServletRequest, username: String, authToken: String) {
//        val userDetails = userDetailsService.loadUserByUsername(username)
//        if (jwtTokenUtil.validateToken(authToken, userDetails)) {
//            val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
//            authentication.details = WebAuthenticationDetailsSource().buildDetails(req)
//            logger.info("Authenticated user '$username', setting security context.")
//            SecurityContextHolder.getContext().authentication = authentication
//        }
//    }
//}