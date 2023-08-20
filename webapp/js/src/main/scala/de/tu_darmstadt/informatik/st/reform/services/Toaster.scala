package de.tu_darmstadt.informatik.st.reform.services

import outwatch.VNode
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import org.scalajs.dom.{document, window}
import scala.scalajs.js
import de.tu_darmstadt.informatik.st.reform.*
import de.tu_darmstadt.informatik.st.reform.given
import de.tu_darmstadt.informatik.st.reform.components.icons
import org.scalajs.dom.HTMLHtmlElement

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
    val copyable: Boolean = false,
) {
  case Default
      extends ToastType(
        "bg-white dark:bg-gray-600 dark:text-gray-200",
        "bg-slate-100 dark:bg-gray-700",
        "",
        Some(icons.Info(cls := "w-6 h-6 text-slate-600 dark:text-gray-200")),
      )
  case Success
      extends ToastType(
        "bg-green-100",
        "bg-green-200",
        "text-green-600",
        Some(icons.CheckCircle(cls := "w-6 h-6 text-green-600")),
      )
  case Warning
      extends ToastType(
        "bg-yellow-100",
        "bg-yellow-200",
        "text-yellow-600",
        Some(icons.WarningTriangle(cls := "w-6 h-6 text-yellow-600")),
      )
  case Error
      extends ToastType(
        "bg-red-100 toast-error",
        "bg-red-200",
        "text-red-600",
        Some(icons.WarningPolygon(cls := "w-6 h-6 text-red-600")),
        true,
      )
}

class Toast(using toaster: Toaster)(
    val text: VNode,
    val toastMode: ToastMode,
    val toastType: ToastType,
    val onclose: (Toast) => Unit,
) {
  val id = js.Math.round(js.Math.random() * 100000)
  var start: Option[Double] = None
  var previousTimeStamp: Double = 0
  var pausedAtTimeStamp: Double = 0
  var animationDone = false
  var animationRef: Option[Int] = None

  private def animate(timestamp: Double, resumeTo: Double = 0): Unit = {
    val element = Option(document.querySelector(s"#toast-$id").asInstanceOf[HTMLHtmlElement]);
    if (resumeTo > 0) {
      start match {
        case None             => {}
        case Some(startValue) => start = Some(startValue + timestamp - resumeTo)
      }
    }

    element match {
      case Some(element) => {
        start match {
          case None => {
            start = Some(timestamp)
            window.requestAnimationFrame(t => animate(t))
          }
          case Some(startValue) => {
            val elapsed = timestamp - startValue

            if (previousTimeStamp != timestamp) {
              // animation magic
              val widthVal = js.Math.min((100 / toastMode.duration.toDouble) * elapsed, 100)
              val width = s"${widthVal}%"
              element.querySelector(".toast-progress").asInstanceOf[HTMLHtmlElement].style.width = width

              if (widthVal >= 100) {
                this.onclose(this)
              }
            }

            if (elapsed < toastMode.duration) {
              previousTimeStamp = timestamp
              if (!animationDone) {
                animationRef = Some(window.requestAnimationFrame(t => animate(t)))
              }
            }
          }
        }
      }
      case None => {
        animationRef = Some(window.requestAnimationFrame(t => animate(t)))
      }
    }
  }

  if (toastMode.autodismiss) {
    animationRef = Some(window.requestAnimationFrame(t => animate(t)))
  }

  def render: VMod = {
    val killTimer =
      if (toastMode.closeable) Some(window.setTimeout(() => { this.onclose(this) }, toastMode.duration)) else None

    div(
      cls := s"${toastType.primaryBgClass} ${toastType.textClass} toast-elem shadow-md alert relative overflow-hidden w-fit",
      onMouseEnter.foreach(_ => {
        killTimer.map(window.clearTimeout(_))
        animationRef match {
          case Some(ref) => {
            pausedAtTimeStamp = previousTimeStamp
            window.cancelAnimationFrame(ref)
          }
          case None => {}
        }
      }),
      onMouseLeave.foreach(_ =>
        animationRef match {
          case Some(ref) => {
            animationRef = Some(window.requestAnimationFrame(t => animate(t, pausedAtTimeStamp)))
          }
          case None => {}
        },
      ),
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
        cls := s"flex flex-row items-start !mt-0 !z-[50] ${if (!toastMode.autodismiss) "!w-full" else ""}",
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
          if (toastType.copyable) {
            Some(
              div(
                icons.Clipboard(cls := "w-4 h-4 text-red-600"),
                cls := "tooltip tooltip-left hover:bg-red-200 rounded-md p-0.5 h-fit w-fit cursor-pointer shrink-0 m-0.5",
                onClick.foreach(_ =>
                  window.navigator.clipboard
                    .writeText(document.querySelector(s"#toast-$id").innerText)
                    .toFuture
                    .onComplete(value => {
                      if (value.isFailure) {
                        println("could not copy to clipboard")
                      } else {
                        toaster.make("Copied to Clipboard!", ToastMode.Short, ToastType.Success)
                      }
                    }),
                ),
              ),
            )
          } else None
        }, {
          if (toastMode.closeable) {
            Some(
              div(
                icons.Close(cls := "text-red-600 w-4 h-4"),
                cls := "tooltip tooltip-left hover:bg-red-200 rounded-md p-0.5 h-fit w-fit cursor-pointer shrink-0 m-0.5 reform-toast-close",
                onClick.foreach(_ => onclose(this)),
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
    if (Globals.VITE_SELENIUM && style != ToastType.Error) return
    val toast = new Toast(using this)(text, mode, style, (t: Toast) => { this.removeToast.fire(t) })
    this.addToast.fire(toast);
  }

  def render: VMod = {
    div(
      cls := "toast toast-end items-end !p-0 bottom-4 right-4",
      Signal { toasts.value.map(toast => { toast.render }) },
    )
  }
}
