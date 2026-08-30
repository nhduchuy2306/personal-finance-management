package com.personalfinance.common.base.response;

/**
 * Singleton response for handlers that don't return data (e.g., delete operations).
 * <p>
 * Usage in handler:
 * <pre>
 * public class DeleteHandler extends AbstractHandler&lt;DeleteRequest, VoidResponse&gt; {
 *     public VoidResponse doHandle(DeleteRequest request) {
 *         repository.deleteById(request.getId());
 *         return VoidResponse.INSTANCE;
 *     }
 * }
 * </pre>
 */
public final class VoidResponse implements BaseResponse {

  public static final VoidResponse INSTANCE = new VoidResponse();

  private VoidResponse() {
  }
}
