#!/usr/bin/perl

use CGI;
use DBI;
use DBD::mysql;

my $dbh=DBI->connect("DBI:mysql:database=emcmarket;host=localhost", "emcmarket", "NE6xYRe6UPwW3Ray", {AutoCommit => 0 });

my $cgi=CGI->new;
my $rows=0;
if ($cgi->param("upload")) {
	my $name=$cgi->param("name");
	my $sthdel=$dbh->prepare(qq(
		delete from signs
		where server=? and x=? and y=? and z=?
	));
	my $sthins=$dbh->prepare(qq(
		insert into signs (server, x, y, z, amount, buy, sell, owner, item, resnumber, chooseposition, lastseen, uploader)
		values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
	));
	foreach my $row (split("\n", $cgi->param("upload"))) {
		my ($server, $x, $y, $z, $amount, $buy, $sell, $owner, $item, $resnumber, $choosepos, $lastseen)=
			split(":", $row);
		$sthdel->execute($server, $x, $y, $z);
		$sthins->execute($server, $x, $y, $z, $amount, $buy, $sell, $owner, $item, $resnumber, $choosepos, $lastseen, $name);
		$rows++;
	}
	$dbh->commit;
	open(F, ">>/tmp/uploaders.dat");
	print F scalar localtime, "  ",  $rows ," uploaded by ", $cgi->param("name"), "\n";
	close F;
}

print qq(Content-type: text/plain\n);
print qq(\n);
print "$rows rows uploaded.\n";
