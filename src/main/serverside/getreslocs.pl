#!/usr/bin/perl
use 5.10.0;

use JSON;
my @reses=();

say "package de.guntram.mcmod.emcshoplocator;";
say "//Warning - this file was generated automatically. Do not edit.";
say "//If you really really need to edit this, preserve the xy sorting!";
say "import java.util.HashMap;";
say "public class ResInit {";
say "public static final HashMap<String, ResPosition[]> positions;";

foreach my $server (1..9) {
	getreslocs("smp".$server);
}
getreslocs("utopia");
say "static {";
say "positions=new HashMap<String, ResPosition[]>();";
foreach my $server (1..9) {
	say("positions.put(\"smp$server\", resof_smp$server());");
}
	say("positions.put(\"utopia\", resof_utopia());");
say "};}";

sub getreslocs {
	my $server=shift;
	my $json=`wget -O - https://$server.emc.gs/tiles/_markers_/marker_town.json`;
	my $perl=from_json($json);
	my @reses=();
	foreach $key (keys %{$perl->{sets}->{"residence.markerset"}->{areas}}) {
		my $resinfo=$perl->{sets}->{'residence.markerset'}->{areas}->{$key};
		# print "$key -> $resinfo\n";
		$address=$resinfo->{desc};
		if ($address=~/Address: (\d+)/) {
			$address=$1;
			# print "$server $key $resinfo->{x}[0] $resinfo->{x}[2] $resinfo->{z}[0] $resinfo->{z}[1] $address\n";
			push(@reses, sprintf("/* sort: %6d %6d %s */ %s,", $resinfo->{x}[0]+100000, $resinfo->{z}[0]+100000, $server, "  new ResPosition(\"$server\", $resinfo->{x}[0], $resinfo->{x}[2], $resinfo->{z}[0], $resinfo->{z}[1], $address)"));
		}
	}
	say("private static ResPosition[] resof_$server() {");
	say "return  new ResPosition[] {";
	foreach $i (sort @reses) { say $i; }
	say "};}";
}
