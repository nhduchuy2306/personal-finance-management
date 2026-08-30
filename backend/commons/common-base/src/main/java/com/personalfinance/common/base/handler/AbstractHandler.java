package com.personalfinance.common.base.handler;

import com.personalfinance.common.base.request.BaseRequest;
import com.personalfinance.common.base.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.ClassUtils;

/**
 * Abstract base Handler with template method pattern.
 * <p>
 * Subclasses MUST override: {@link #doHandle(BaseRequest)}.
 * Subclasses MAY override: {@link #preHandle(BaseRequest)}, {@link #postHandle(BaseRequest, BaseResponse)}.
 * {@link #getRequestType()} is auto-resolved from generic type arguments — no need to override.
 * <p>
 * The {@link #execute(BaseRequest)} method orchestrates the full flow.
 *
 * @param <Req> Request DTO type — must implement {@link BaseRequest}
 * @param <Res> Response DTO type — must implement {@link BaseResponse}
 */
@Slf4j
public abstract class AbstractHandler<Req extends BaseRequest, Res extends BaseResponse>
  implements Handler<Req, Res> {

  /**
   * Template method — orchestrates the full handler flow.
   * Call this from controllers/consumers, NOT doHandle directly.
   */
  public Res execute(Req request) {
    preHandle(request);
    Res response = doHandle(request);
    try {
      postHandle(request, response);
    } catch (Exception e) {
      // postHandle failures are logged but don't rollback the main operation
      log.error("postHandle failed for {}: {}", getRequestType().getSimpleName(), e.getMessage(), e);
    }
    return response;
  }

  @Override
  public void preHandle(Req request) {
    // default no-op — override when needed
  }

  @Override
  public void postHandle(Req request, Res response) {
    // default no-op — override when needed
  }

  /**
   * Auto-resolved from generic type arguments.
   * Uses Spring's GenericTypeResolver with CGLIB-proxy safety via ClassUtils.getUserClass().
   * Override only if auto-resolution fails (e.g., deeply nested generics).
   */
  @Override
  @SuppressWarnings("unchecked")
  public Class<Req> getRequestType() {
    Class<?> userClass = ClassUtils.getUserClass(this);
    Class<?>[] typeArgs = GenericTypeResolver.resolveTypeArguments(userClass, AbstractHandler.class);
    if (typeArgs == null || typeArgs.length == 0) {
      throw new IllegalStateException(
        "Cannot resolve request type for " + userClass.getSimpleName()
          + ". Ensure concrete type arguments are specified when extending AbstractHandler.");
    }
    return (Class<Req>) typeArgs[0];
  }
}
