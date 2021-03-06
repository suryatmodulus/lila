package lila.app
package http

import play.api.i18n.Lang
import play.api.mvc._
import scala.concurrent.duration._

import lila.common.HTTPRequest
import lila.api.Context
import lila.i18n.I18nLangPicker

final class PageCache(security: lila.security.SecurityApi, cacheApi: lila.memo.CacheApi) {

  private val cache = cacheApi.notLoading[String, Result](16, "pageCache") {
    _.expireAfterWrite(1.seconds).buildAsync()
  }

  def apply(compute: () => Fu[Result])(implicit ctx: Context): Fu[Result] =
    if (ctx.isAnon && langs(ctx.lang.language)) {
      val cacheKey = s"${HTTPRequest actionName ctx.req}(${ctx.lang.language})"
      cache.getFuture(cacheKey, _ => compute())
    } else
      compute()

  private val langs =
    Set("en", "ru", "tr", "de", "es", "fr", "pt", "it", "pl", "ar", "fa", "id", "nl", "nb", "sv")
}
