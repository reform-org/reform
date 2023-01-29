package webapp.components

import outwatch.VNode
import outwatch.dsl.*
import outwatch.*

object Icons {
  def reload(_cls: String = "", _color: String = "#000000"): VNode = {
    import svg.*
    svg(
      xmlns := "http://www.w3.org/2000/svg",
      fill := _color,
      viewBox := "0 0 24 24",
      cls := _cls,
      path(
        d := "M23,12A11,11,0,1,1,12,1a10.9,10.9,0,0,1,5.882,1.7l1.411-1.411A1,1,0,0,1,21,2V6a1,1,0,0,1-1,1H16a1,1,0,0,1-.707-1.707L16.42,4.166A8.9,8.9,0,0,0,12,3a9,9,0,1,0,9,9,1,1,0,0,1,2,0Z",
      ),
    )
  }

  def reform(_cls: String = ""): VNode = {
    span(
      cls := s"font-roboto-slab $_cls",
      span(
        cls := "text-purple-600 font-extrabold",
        "RE",
      ),
      span(
        cls := "font-light",
        "Form",
      ),
    )
  }

  def connections(_cls: String = "", _color: String = ""): VNode = {
    import svg.*
    svg(
      xmlns := "http://www.w3.org/2000/svg",
      cls := _cls,
      fill := _color,
      viewBox := "0 0 303.374 303.374",
      g(
        path(
          d := "M268.291,177.313c-4.144,0-8.12,0.727-11.814,2.052l-17.319-27.524c10.331-10.171,16.751-24.302,16.751-39.91   c0-30.899-25.138-56.037-56.037-56.037s-56.037,25.138-56.037,56.037c0,12.226,3.947,23.54,10.617,32.762l-33.742,33.954   c-4.438-2.404-9.515-3.771-14.907-3.771c-5.323,0-10.339,1.336-14.736,3.684l-19.721-20.688c5.93-7.037,9.514-16.113,9.514-26.014   c0-22.293-18.137-40.43-40.43-40.43S0,109.565,0,131.858s18.136,40.43,40.429,40.43c5.854,0,11.416-1.261,16.444-3.509   l21.387,22.436c-2.456,4.474-3.856,9.606-3.856,15.06c0,17.313,14.085,31.398,31.398,31.398s31.398-14.085,31.398-31.398   c0-5.388-1.365-10.462-3.766-14.897l33.756-33.969c9.207,6.635,20.491,10.559,32.68,10.559c8.815,0,17.157-2.052,24.584-5.694   l17.197,27.329c-5.258,6.136-8.446,14.097-8.446,22.793c0,19.345,15.739,35.084,35.084,35.084s35.083-15.739,35.083-35.084   S287.636,177.313,268.291,177.313z M161.834,111.931c0-20.974,17.063-38.037,38.037-38.037s38.037,17.063,38.037,38.037   s-17.063,38.037-38.037,38.037S161.834,132.904,161.834,111.931z M105.802,219.673c-7.388,0-13.398-6.011-13.398-13.398   s6.011-13.398,13.398-13.398s13.398,6.011,13.398,13.398S113.19,219.673,105.802,219.673z M18,131.858   c0-12.368,10.062-22.43,22.429-22.43s22.43,10.062,22.43,22.43s-10.062,22.43-22.43,22.43S18,144.226,18,131.858z M268.291,229.48   c-9.42,0-17.084-7.664-17.084-17.084s7.664-17.084,17.084-17.084s17.083,7.664,17.083,17.084S277.71,229.48,268.291,229.48z",
        ),
      ),
    )
  }

  def hamburger(_cls: String = "", _color: String = ""): VNode = {
    import svg.*
    svg(
      xmlns := "http://www.w3.org/2000/svg",
      cls := _cls,
      fill := "none",
      viewBox := "0 0 24 24",
      stroke := "currentColor",
      path(
        VModifier.attr("stroke-linecap") := "round",
        VModifier.attr("stroke-linejoin") := "round",
        VModifier.attr("stroke-width") := "2",
        d := "M4 6h16M4 12h8m-8 6h16",
      ),
    )
  }

  def clipboard(_cls: String = "", _color: String = "", _cls2: String = ""): VNode = {
    import svg.*
    svg(
      xmlns := "http://www.w3.org/2000/svg",
      cls := _cls,
      viewBox := "0 0 24 24",
      fill := "none",
      path(
        d := "M8 12.2H15",
        stroke := _color,
        cls := _cls2,
        VModifier.attr("stroke-width") := "1.5",
        VModifier.attr("stroke-miterlimit") := "10",
        VModifier.attr("stroke-linecap") := "round",
        VModifier.attr("stroke-linejoin") := "round",
      ),
      path(
        d := "M8 16.2H12.38",
        stroke := _color,
        cls := _cls2,
        VModifier.attr("stroke-width") := "1.5",
        VModifier.attr("stroke-miterlimit") := "10",
        VModifier.attr("stroke-linecap") := "round",
        VModifier.attr("stroke-linejoin") := "round",
      ),
      path(
        d := "M10 6H14C16 6 16 5 16 4C16 2 15 2 14 2H10C9 2 8 2 8 4C8 6 9 6 10 6Z",
        stroke := _color,
        cls := _cls2,
        VModifier.attr("stroke-width") := "1.5",
        VModifier.attr("stroke-miterlimit") := "10",
        VModifier.attr("stroke-linecap") := "round",
        VModifier.attr("stroke-linejoin") := "round",
      ),
      path(
        d := "M16 4.02002C19.33 4.20002 21 5.43002 21 10V16C21 20 20 22 15 22H9C4 22 3 20 3 16V10C3 5.44002 4.67 4.20002 8 4.02002",
        stroke := _color,
        cls := _cls2,
        VModifier.attr("stroke-width") := "1.5",
        VModifier.attr("stroke-miterlimit") := "10",
        VModifier.attr("stroke-linecap") := "round",
        VModifier.attr("stroke-linejoin") := "round",
      ),
    )
  }

  def mail(_cls: String = "", _color: String = "", _cls2: String = ""): VNode = {
    import svg.*
    svg(
      xmlns := "http://www.w3.org/2000/svg",
      cls := _cls,
      viewBox := "0 0 24 24",
      fill := "none",
      path(
        d := "M3 8L10.8906 13.2604C11.5624 13.7083 12.4376 13.7083 13.1094 13.2604L21 8M5 19H19C20.1046 19 21 18.1046 21 17V7C21 5.89543 20.1046 5 19 5H5C3.89543 5 3 5.89543 3 7V17C3 18.1046 3.89543 19 5 19Z",
        stroke := _color,
        cls := _cls2,
        VModifier.attr("stroke-width") := "2",
        VModifier.attr("stroke-linecap") := "round",
        VModifier.attr("stroke-linejoin") := "round",
      ),
    )
  }

  def whatsapp(_cls: String = "", _color: String = ""): VNode = {
    import svg.*
    svg(
      xmlns := "http://www.w3.org/2000/svg",
      fill := _color,
      cls := _cls,
      viewBox := "0 0 32 32",
      path(
        d := "M26.576 5.363c-2.69-2.69-6.406-4.354-10.511-4.354-8.209 0-14.865 6.655-14.865 14.865 0 2.732 0.737 5.291 2.022 7.491l-0.038-0.070-2.109 7.702 7.879-2.067c2.051 1.139 4.498 1.809 7.102 1.809h0.006c8.209-0.003 14.862-6.659 14.862-14.868 0-4.103-1.662-7.817-4.349-10.507l0 0zM16.062 28.228h-0.005c-0 0-0.001 0-0.001 0-2.319 0-4.489-0.64-6.342-1.753l0.056 0.031-0.451-0.267-4.675 1.227 1.247-4.559-0.294-0.467c-1.185-1.862-1.889-4.131-1.889-6.565 0-6.822 5.531-12.353 12.353-12.353s12.353 5.531 12.353 12.353c0 6.822-5.53 12.353-12.353 12.353h-0zM22.838 18.977c-0.371-0.186-2.197-1.083-2.537-1.208-0.341-0.124-0.589-0.185-0.837 0.187-0.246 0.371-0.958 1.207-1.175 1.455-0.216 0.249-0.434 0.279-0.805 0.094-1.15-0.466-2.138-1.087-2.997-1.852l0.010 0.009c-0.799-0.74-1.484-1.587-2.037-2.521l-0.028-0.052c-0.216-0.371-0.023-0.572 0.162-0.757 0.167-0.166 0.372-0.434 0.557-0.65 0.146-0.179 0.271-0.384 0.366-0.604l0.006-0.017c0.043-0.087 0.068-0.188 0.068-0.296 0-0.131-0.037-0.253-0.101-0.357l0.002 0.003c-0.094-0.186-0.836-2.014-1.145-2.758-0.302-0.724-0.609-0.625-0.836-0.637-0.216-0.010-0.464-0.012-0.712-0.012-0.395 0.010-0.746 0.188-0.988 0.463l-0.001 0.002c-0.802 0.761-1.3 1.834-1.3 3.023 0 0.026 0 0.053 0.001 0.079l-0-0.004c0.131 1.467 0.681 2.784 1.527 3.857l-0.012-0.015c1.604 2.379 3.742 4.282 6.251 5.564l0.094 0.043c0.548 0.248 1.25 0.513 1.968 0.74l0.149 0.041c0.442 0.14 0.951 0.221 1.479 0.221 0.303 0 0.601-0.027 0.889-0.078l-0.031 0.004c1.069-0.223 1.956-0.868 2.497-1.749l0.009-0.017c0.165-0.366 0.261-0.793 0.261-1.242 0-0.185-0.016-0.366-0.047-0.542l0.003 0.019c-0.092-0.155-0.34-0.247-0.712-0.434z",
      ),
    )
  }

  def close(_cls: String = "", _color: String = ""): VNode = {
    import svg.*
    svg(
      xmlns := "http://www.w3.org/2000/svg",
      viewBox := "0 0 1024 1024",
      fill := _color,
      cls := _cls,
      path(
        d := "M176.662 817.173c-8.19 8.471-7.96 21.977 0.51 30.165 8.472 8.19 21.978 7.96 30.166-0.51l618.667-640c8.189-8.472 7.96-21.978-0.511-30.166-8.471-8.19-21.977-7.96-30.166 0.51l-618.666 640z",
        fill := "",
      ),
      path(
        d := "M795.328 846.827c8.19 8.471 21.695 8.7 30.166 0.511 8.471-8.188 8.7-21.694 0.511-30.165l-618.667-640c-8.188-8.471-21.694-8.7-30.165-0.511-8.471 8.188-8.7 21.694-0.511 30.165l618.666 640z",
        fill := "",
      ),
    )
  }

  def ghost(_cls: String = ""): VNode = {
    import svg.*
    svg(
      xmlns := "http://www.w3.org/2000/svg",
      viewBox := "0 0 512.003 512.003",
      cls := _cls,
      path(
        styleAttr := "fill:#E0E0E3;",
        d := "M59.074,472.615V196.927C59.074,88.167,147.242,0,256,0h0.003  c108.76,0,196.927,88.167,196.927,196.927v273.116v1.259c0,18.328-11.934,35.343-29.771,39.562  c-25.596,6.056-48.422-12.877-48.989-37.314c-0.456-19.688-14.642-37.471-34.172-39.98c-23.321-2.999-43.312,14.423-44.549,36.844  c-1.111,20.14-14.226,38.687-34.232,41.255c-23.317,2.992-43.301-14.429-44.538-36.848c-1.111-20.14-14.226-38.685-34.232-41.254  c-23.394-3.002-43.434,14.541-44.55,37.065c-0.979,19.716-13.674,37.891-33.186,40.89C80.188,515.287,59.074,496.417,59.074,472.615  z",
      ),
      g(
        path(
          styleAttr := "fill:#C7C5CC;",
          d := "M279.849,433.567c-23.321-2.999-43.312,14.423-44.549,36.844c-0.55,9.982-4.053,19.569-9.877,27.016   c8.224,10.124,21.368,16.09,35.792,14.239c20.007-2.568,33.121-21.115,34.232-41.255c0.479-8.675,3.774-16.594,8.974-22.879   C298.354,440.092,289.799,434.845,279.849,433.567z",
        ),
        path(
          styleAttr := "fill:#C7C5CC;",
          d := "M122.299,433.565c-23.394-3.002-43.434,14.541-44.55,37.065c-0.493,9.939-3.967,19.484-9.787,26.892   c8.427,10.306,21.964,16.271,36.747,13.998c19.512-2.999,32.207-21.174,33.186-40.89c0.428-8.635,3.645-16.531,8.758-22.826   C140.725,440.223,132.391,434.86,122.299,433.565z",
        ),
        path(
          styleAttr := "fill:#C7C5CC;",
          d := "M256.002,0h-0.001c-10.225,0-20.267,0.781-30.072,2.283   c94.485,14.478,166.854,96.109,166.854,194.644v273.116v1.259c0,9.85-3.451,19.316-9.408,26.625   c9.094,10.828,23.903,16.693,39.783,12.937c17.837-4.219,29.771-21.233,29.771-39.562v-1.259V196.927   C452.929,88.167,364.763,0,256.002,0z",
        ),
      ),
      g(
        circle(styleAttr := "fill:#FFD92D;", cx := "184.232", cy := "216.017", r := "46.386"),
        circle(styleAttr := "fill:#FFD92D;", cx := "324.922", cy := "216.017", r := "46.387"),
      ),
      g(
        circle(styleAttr := "fill:#956EC4;", cx := "342.6", cy := "234.362", r := "21.644"),
        circle(styleAttr := "fill:#956EC4;", cx := "199.648", cy := "235.068", r := "21.644"),
      ),
    )
  }
}
