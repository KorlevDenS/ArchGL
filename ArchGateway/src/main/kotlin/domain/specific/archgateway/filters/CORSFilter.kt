package domain.specific.archgateway.filters

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.FilterConfig
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component


@Component
class CORSFilter: Filter {

    override fun doFilter(
        request: ServletRequest?,
        response: ServletResponse?,
        chain: FilterChain?
    ) {
        val httpResponse = response as HttpServletResponse?
        httpResponse?.setHeader("Access-Control-Allow-Origin", "*")
        httpResponse?.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        httpResponse?.setHeader("Access-Control-Allow-Headers", "*")
        chain?.doFilter(request, response)
    }

    override fun destroy() {}

}
