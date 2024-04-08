package lila.report

import scalalib.Iso
import lila.user.Me

sealed trait Reason:

  def key = toString.toLowerCase

  def name = toString

object Reason:

  case object Cheat extends Reason
  case object CheatPrint extends Reason: // BC, replaced with AltPrint
    override def name = "Print"
  case object AltPrint extends Reason:
    override def name = "Print"
  case object Comm extends Reason:
    def flagText = "[FLAG]"
  case object Boost    extends Reason
  case object Username extends Reason
  case object Sexism   extends Reason
  case object Other    extends Reason
  case object Playbans extends Reason

  val all       = List(Cheat, AltPrint, Comm, Boost, Username, Sexism, Other, CheatPrint)
  val keys      = all.map(_.key)
  val byKey     = all.mapBy(_.key)
  val autoBlock = Set(Comm, Sexism)

  given Iso.StringIso[Reason] = Iso.string(k => byKey.getOrElse(k, Other), _.key)

  def apply(key: String): Option[Reason] = byKey.get(key)

  trait WithReason:
    def reason: Reason

    def isCheat                           = reason == Cheat
    def isOther                           = reason == Other
    def isPrint                           = reason == AltPrint || reason == CheatPrint
    def isComm                            = reason == Comm
    def isBoost                           = reason == Boost
    def is(reason: Reason.type => Reason) = this.reason == reason(Reason)

  def isGranted(reason: Reason)(using Me) =
    import lila.core.perm.Granter
    reason match
      case Cheat                                               => Granter[Me](_.MarkEngine)
      case Comm | Sexism                                       => Granter[Me](_.Shadowban)
      case Boost                                               => Granter[Me](_.MarkBooster)
      case AltPrint | CheatPrint | Playbans | Username | Other => Granter[Me](_.Admin)
