#!/usr/bin/perl

use CGI qw(:all);
use DBI;
use DBD::mysql;


my $q=CGI->new;

print $q->header();
print $q->start_html("EMC Shop query");
print $q->h1("EMC Shop query");
print start_form();
print textfield(-name => "item", -default => "cobblestone", -size => 50, -maxlength => 50);
print "<br>";
print submit();
print end_form;

if ($q->param("item")) {
	my $dbh=DBI->connect("DBI:mysql:database=emcmarket;host=localhost", "emcmarket", "NE6xYRe6UPwW3Ray");
	my $sth=$dbh->prepare(qq(
		select server, resnumber, x, y, z, 
		amount, buy, sell, owner, item
		from signs
		where lower(item) like ? 
		order by item, buy/amount, amount
	));
	$sth->execute("%".lc $q->param("item") . "%");
	print $q->start_table();
	print $q->Tr($q->th([
		"server", "res", "amount", "buy", "sell", "buy per item", "sell per item", "player", "item"
	]));
	while (my(@f)=$sth->fetchrow_array()) {
		unless ($had_by_owner{$f[8].$f[9].$f[5]}) {
			$had_by_owner{$f[8].$f[9].$f[5]}=1;
			print $q->Tr($q->td([
				$f[0],
				$f[1],
				$f[5],
				$f[6]==-1 ? "" : $f[6],
				$f[7]==-1 ? "" : $f[7],
				$f[6]==-1 ? "" : sprintf("%.2f", $f[6]/$f[5]),
				$f[7]==-1 ? "" : sprintf("%.2f", $f[7]/$f[5]),
				$f[8],
				$f[9]
			]));
		}
	}
	print $q->end_table();
}
print $q->end_html;

