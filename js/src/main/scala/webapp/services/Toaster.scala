package webapp.services

import outwatch.VNode
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import org.scalajs.dom.{console, document, window}
import scala.scalajs.js
import webapp.*
import webapp.given
import webapp.components.Icons
import org.scalajs.dom.Element
import org.scalajs.dom.HTMLHtmlElement

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
    val text: String,
    val autodismiss: Boolean,
    val autoDismissAfter: Int,
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
          window.requestAnimationFrame(animate)
        }
        case Some(startValue) => {
          val elapsed = timestamp - startValue;

          if (previousTimeStamp != timestamp) {
            // animation magic
            val width = s"${js.Math.min((100 / autoDismissAfter.toDouble) * elapsed, 100)}%"
            element.querySelector(".toast-progress").asInstanceOf[HTMLHtmlElement].style.width = width
          }

          if (elapsed < autoDismissAfter) {
            previousTimeStamp = timestamp
            if (!animationDone) {
              window.requestAnimationFrame(animate)

            }
          }
        }
      }
    } else {
      window.requestAnimationFrame(animate)
    }
  }

  if (autodismiss) {
    window.requestAnimationFrame(animate)
  }

  def render: VNode = {
    div(
      cls := s"${toastType.primaryBgClass} ${toastType.textClass} shadow-md alert relative overflow-hidden",
      idAttr := s"toast-$id", {
        if (autodismiss)
          Some(
            div(
              cls := s"toast-progress absolute w-0 h-full left-0 ${toastType.secondaryBgClass}",
            ),
          )
        else
          None
      },
      div(
        cls := "z-50",
        toastType.icon match {
          case None       => ""
          case Some(icon) => icon
        },
        span(text),
        div(
          Icons.close("fill-red-600 w-4 h-4"),
          cls := "tooltip tooltip-left hover:bg-red-200 rounded-md p-0.5 h-fit w-fit cursor-pointer",
          onClick.foreach(_ => onclose(this)),
        ),
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

  def make(text: String, autodismiss: Boolean, style: ToastType = ToastType.Default): Unit = {
    val dismissAfter = 10000;
    val toast = new Toast(text, autodismiss, dismissAfter, style, (t: Toast) => { this.removeToast(t) })
    this.addToast(toast);

    if (autodismiss) {
      val interval = window.setTimeout(
        () => {
          this.removeToast(toast)
        },
        dismissAfter,
      )
    }
  }

  def render: VNode = {
    div(
      cls := "toast toast-end",
      toasts.map(_.map(toast => { toast.render })),
    )
  }
}
