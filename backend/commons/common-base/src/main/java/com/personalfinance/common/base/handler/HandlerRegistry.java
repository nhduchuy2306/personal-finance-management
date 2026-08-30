package com.personalfinance.common.base.handler;

import com.personalfinance.common.base.exception.HandlerNotFoundException;
import com.personalfinance.common.base.request.BaseRequest;
import com.personalfinance.common.base.response.BaseResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Mediator that dispatches requests to the correct Handler
 * based on request DTO class type.
 * <p>
 * All requests must implement {@link BaseRequest}.
 * All responses must implement {@link BaseResponse}.
 */
@Component
public class HandlerRegistry {
  private final Map<Class<?>, Handler<?, ?>> handlers;

  public HandlerRegistry(List<Handler<?, ?>> handlerList) {
    this.handlers = handlerList.stream()
      .collect(Collectors.toMap(
        Handler::getRequestType,
        Function.identity(),
        (existing, duplicate) -> {
          throw new IllegalStateException(
            "Duplicate handler for request type: " + existing.getRequestType().getSimpleName()
          );
        }
      ));
  }

  /**
   * Dispatch a request to its registered handler.
   *
   * @throws HandlerNotFoundException if no handler is registered for the request type
   */
  @SuppressWarnings("unchecked")
  public <Req extends BaseRequest, Res extends BaseResponse> Res dispatch(Req request) {
    Handler<Req, Res> handler = (Handler<Req, Res>) handlers.get(request.getClass());
    if (handler == null) {
      throw new HandlerNotFoundException(
        "No handler registered for: " + request.getClass().getSimpleName()
      );
    }
    return ((AbstractHandler<Req, Res>) handler).execute(request);
  }
}
