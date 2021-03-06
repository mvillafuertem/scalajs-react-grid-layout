package react

import scala.scalajs.js
import js.JSConverters._
import japgolly.scalajs.react.Callback
import org.scalajs.dom.html.{ Element => HTMLElement }
import org.scalajs.dom.MouseEvent
import org.scalajs.dom.raw.Event
import react.common.EnumValue

package object gridlayout {
  type Margin           = (Int, Int)
  type ContainerPadding = (Int, Int)

  // Callbacks
  type OnLayoutChange     = Layout => Callback
  type OnLayoutsChange    = (Layout, Layouts) => Callback
  type OnBreakpointChange = (BreakpointName, Int) => Callback
  type OnWidthChange      = (Int, Margin, Int, ContainerPadding) => Callback
  type ItemCallback       =
    (Layout, LayoutItem, LayoutItem, Option[LayoutItem], MouseEvent, HTMLElement) => Callback
  type DropCallback       =
    (Layout, LayoutItem, Event) => Callback
}

package gridlayout {
  trait BreakpointName    {
    val name: String
  }

  object BreakpointName {
    private final case class BreakpointNameI(name: String) extends BreakpointName
    def apply(name: String): BreakpointName = new BreakpointNameI(name)

    val xxl: BreakpointName = apply("xxl")
    val xl: BreakpointName  = apply("xl")
    val lg: BreakpointName  = apply("lg")
    val md: BreakpointName  = apply("md")
    val sm: BreakpointName  = apply("sm")
    val xs: BreakpointName  = apply("xs")
    val xxs: BreakpointName = apply("xxs")

    val predefined: List[BreakpointName] = List(xxl, xl, lg, md, sm, xs, xxs)
  }

  final case class Breakpoint(name: BreakpointName, pos: Int)
  final case class Breakpoints(bps: List[Breakpoint]) {
    def toRaw: js.Object = {
      val p = js.Dynamic.literal()
      bps.foreach { case Breakpoint(name, v) => p.updateDynamic(name.name)(v.asInstanceOf[js.Any]) }
      p
    }
  }

  final case class Column(col: BreakpointName, pos: Int)
  final case class Columns(cols: List[Column]) {
    def toRaw: js.Object = {
      val p = js.Dynamic.literal()
      cols.foreach { case Column(name, v) => p.updateDynamic(name.name)(v.asInstanceOf[js.Any]) }
      p
    }
  }

  final case class DroppingItem(
    i: String,
    w: Int,
    h: Int
  ) {
    def toRaw: raw.DroppingItem = {
      val p = (new js.Object).asInstanceOf[raw.DroppingItem]
      p.i = i
      p.w = w
      p.h = h
      p
    }
  }

  final case class BreakpointLayout(name: BreakpointName, layout: Layout)

  object BreakpointLayout {
    private[gridlayout] def layoutsFromRaw(l: js.Object): Layout = {
      val c                   = l.asInstanceOf[js.Array[raw.LayoutItem]]
      val i: List[LayoutItem] = c.map(LayoutItem.fromRaw).toList
      Layout(i)
    }
  }

  final case class Layouts(layouts: List[BreakpointLayout]) {
    def toRaw: js.Object = {
      val p = js.Dynamic.literal()
      layouts.foreach { case BreakpointLayout(name, v) => p.updateDynamic(name.name)(v.toRaw) }
      p
    }
  }

  object Layouts {
    private[gridlayout] def fromRaw(l: js.Object): Layouts = {
      val c  = l.asInstanceOf[js.Dictionary[js.Any]]
      val bp = for {
        p <- js.Object.getOwnPropertyNames(l)
        v <- c.get(p)
      } yield BreakpointLayout(BreakpointName(p),
                               BreakpointLayout.layoutsFromRaw(v.asInstanceOf[js.Object])
      )
      Layouts(bp.toList)
    }

  }

  final case class LayoutItem(
    w:             Int,
    h:             Int,
    x:             Int,
    y:             Int,
    i:             js.UndefOr[String] = js.undefined,
    minW:          js.UndefOr[Int] = js.undefined,
    minH:          js.UndefOr[Int] = js.undefined,
    maxW:          js.UndefOr[Int] = js.undefined,
    maxH:          js.UndefOr[Int] = js.undefined,
    static:        js.UndefOr[Boolean] = js.undefined,
    isDraggable:   js.UndefOr[Boolean] = js.undefined,
    isResizable:   js.UndefOr[Boolean] = js.undefined,
    resizeHandles: js.UndefOr[List[String]] = js.undefined,
    isBounded:     js.UndefOr[Boolean] = js.undefined
  ) {
    def toRaw: raw.LayoutItem =
      new raw.LayoutItem(w,
                         h,
                         x,
                         y,
                         i,
                         minW,
                         minH,
                         maxW,
                         maxH,
                         static,
                         isDraggable,
                         isResizable,
                         resizeHandles.map(_.toJSArray),
                         isBounded
      )
  }

  object LayoutItem {
    private[gridlayout] def fromRaw(l: raw.LayoutItem): LayoutItem =
      new LayoutItem(l.w,
                     l.h,
                     l.x,
                     l.y,
                     l.i,
                     l.minW,
                     l.minH,
                     l.maxW,
                     l.maxH,
                     l.static,
                     l.isDraggable,
                     l.isResizable,
                     l.resizeHandles.map(_.toList),
                     l.isBounded
      )

    private[gridlayout] def fromRawO(l: raw.LayoutItem): Option[LayoutItem] =
      if (l != null)
        Some(
          new LayoutItem(l.w,
                         l.h,
                         l.x,
                         l.y,
                         l.i,
                         l.minW,
                         l.minH,
                         l.maxW,
                         l.maxH,
                         l.static,
                         l.isDraggable,
                         l.isResizable,
                         l.resizeHandles.map(_.toList),
                         l.isBounded
          )
        )
      else
        None
  }

  final case class Layout(l: List[LayoutItem]) {
    private[gridlayout] def toRaw: raw.Layout = l.toArray.map(_.toRaw).toJSArray
  }

  object Layout {
    val Empty: Layout = Layout(Nil)

    private[gridlayout] def fromRaw(l: raw.Layout): Layout =
      Layout(List(l.map(LayoutItem.fromRaw).toSeq: _*))
  }

  sealed trait CompactType extends Product with Serializable
  object CompactType {
    implicit val enum: EnumValue[CompactType] = EnumValue.toLowerCaseString
    case object Vertical   extends CompactType
    case object Horizontal extends CompactType
  }

}
