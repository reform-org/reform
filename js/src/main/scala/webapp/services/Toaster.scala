package webapp.services

import outwatch.VNode
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import org.scalajs.dom.{document, window}
import scala.scalajs.js
import webapp.*
import webapp.given
import webapp.components.Icons
import org.scalajs.dom.HTMLHtmlElement
import scala.annotation.nowarn

enum ToastMode(val autodismiss: Boolean, val closeable: Boolean, val duration: Int = 0) {
  case Short extends ToastMode(true, true, 10000)
  case Long extends ToastMode(true, true, 20000)
  case Infinit extends ToastMode(false, true)
  case Persistent extends ToastMode(false, false)
}

enum ToastType(
    val primaryBgClass: String,
    val secondaryBgClass: String,
    val textClass: String,
    val icon: Option[VNode],
) {
  case Default extends ToastType("bg-white", "bg-slate-100", "", Some(Icons.infoStar("w-6 h-6", "#475569")))
  case Success
      extends ToastType(
        "bg-green-100",
        "bg-green-200",
        "text-green-600",
        Some(Icons.checkmarkCircle("w-6 h-6", "#16A34A")),
      )
  case Warning
      extends ToastType(
        "bg-yellow-100",
        "bg-yellow-200",
        "text-yellow-600",
        Some(Icons.warningTriangle("w-6 h-6", "#ca8a04")),
      )
  case Error
      extends ToastType("bg-red-100", "bg-red-200", "text-red-600", Some(Icons.warningPolygon("w-6 h-6 fill-red-600")))
}

class Toast(
    val text: VNode,
    val toastMode: ToastMode,
    val toastType: ToastType,
    val onclose: (Toast) => Unit,
) {
  val id = js.Math.round(js.Math.random() * 100000)
  var start: Option[Double] = None
  var previousTimeStamp: Double = 0
  var animationDone = false

  private def animate(timestamp: Double): Unit = {
    val element = document.querySelector(s"#toast-$id").asInstanceOf[HTMLHtmlElement];
    if (element != null) {
      start match {
        case None => {
          start = Some(timestamp)
          window.requestAnimationFrame(t => animate(t)): @nowarn
        }
        case Some(startValue) => {
          val elapsed = timestamp - startValue;

          if (previousTimeStamp != timestamp) {
            // animation magic
            val width = s"${js.Math.min((100 / toastMode.duration.toDouble) * elapsed, 100)}%"
            element.querySelector(".toast-progress").asInstanceOf[HTMLHtmlElement].style.width = width
          }

          if (elapsed < toastMode.duration) {
            previousTimeStamp = timestamp
            if (!animationDone) {
              window.requestAnimationFrame(t => animate(t)): @nowarn

            }
          }
        }
      }
    } else {
      window.requestAnimationFrame(t => animate(t)): @nowarn
    }
  }

  if (toastMode.autodismiss) {
    window.requestAnimationFrame(t => animate(t)): @nowarn
  }

  def render: VNode = {
    div(
      cls := s"${toastType.primaryBgClass} ${toastType.textClass} shadow-md alert relative overflow-hidden w-fit",
      idAttr := s"toast-$id", {
        if (toastMode.autodismiss)
          Some(
            div(
              cls := s"toast-progress absolute w-0 h-full left-0 top-0 ${toastType.secondaryBgClass}",
            ),
          )
        else
          None
      },
      div(
        cls := s"z-50 flex flex-row items-start !mt-0 ${if (!toastMode.autodismiss) "!w-full" else ""}",
        div(
          cls := "shrink-0",
          toastType.icon match {
            case None       => ""
            case Some(icon) => icon
          },
        ),
        div(
          cls := "",
          text,
        ), {
          if (toastMode.closeable) {
            Some(
              div(
                Icons.close("fill-red-600 w-4 h-4"),
                cls := "tooltip tooltip-left hover:bg-red-200 rounded-md p-0.5 h-fit w-fit cursor-pointer shrink-0 m-0.5",
                onClick.foreach(_ => onclose(this)),
                idAttr := "close",
              ),
            )
          } else None
        },
      ),
    )
  }
}

class Toaster() {

  private val removeToast = Evt[Toast]()
  private val addToast = Evt[Toast]()
  private val addToastB = addToast.act(current[Seq[Toast]] :+ _)
  private val removeToastB = removeToast.act(r => current[Seq[Toast]].filter(b => !b.equals(r)))

  val toasts = Fold(Seq.empty: Seq[Toast])(addToastB, removeToastB)

  def make(text: String, mode: ToastMode = ToastMode.Short, style: ToastType = ToastType.Default): Unit = {
    this.make(span(text), mode, style)
  }

  def make(text: VNode, mode: ToastMode, style: ToastType): Unit = {
    val toast = new Toast(text, mode, style, (t: Toast) => { this.removeToast.fire(t) })
    this.addToast.fire(toast);

    if (mode.autodismiss) {
      window.setTimeout(
        () => {
          this.removeToast(toast)
        },
        mode.duration,
      ): @nowarn
    }
  }

  def render: VNode = {
    div(
      cls := "toast toast-end items-end",
      toasts.map(_.map(toast => { toast.render })),
    )
  }
}
